const { initializeTestEnvironment, assertFails, assertSucceeds } = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

const PROJECT_ID = "demo-no-project";
const DATABASE_ID = "ai-studio-android-campuspl-12c984ef-ad1f-4339-82f0-dcf70fff55b1";

let testEnv;

before(async () => {
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules: fs.readFileSync("firestore.rules", "utf8"),
      host: "127.0.0.1",
      port: 8085
    }
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("unauthenticated client cannot read or write user profiles", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore(DATABASE_ID);
  const profileRef = unauthDb.collection("users").doc("student123");
  await assertFails(profileRef.get());
  await assertFails(profileRef.set({ email: "student@wits.ac.za", displayName: "Lethabo", campusCode: "wits" }));
});

test("student cannot register themselves as already VERIFIED or admin", async () => {
  const studentDb = testEnv.authenticatedContext("student_cheater", { email: "student@uj.ac.za" }).firestore(DATABASE_ID);
  const profileRef = studentDb.collection("users").doc("student_cheater");
  
  // Attempting to register as already VERIFIED must fail
  await assertFails(profileRef.set({
    email: "student@uj.ac.za",
    displayName: "Cheater",
    campusCode: "uj",
    verificationStatus: "VERIFIED",
    isStudentVerified: true
  }));

  // Attempting to register as isAdmin must fail
  await assertFails(profileRef.set({
    email: "student@uj.ac.za",
    displayName: "Cheater",
    campusCode: "uj",
    isAdmin: true
  }));
});

test("student can register with NOT_SUBMITTED verification status", async () => {
  const studentDb = testEnv.authenticatedContext("student_legit", { email: "student@uj.ac.za" }).firestore(DATABASE_ID);
  const profileRef = studentDb.collection("users").doc("student_legit");
  
  await assertSucceeds(profileRef.set({
    email: "student@uj.ac.za",
    displayName: "Thabo Mokoena",
    institutionName: "University of Johannesburg",
    campusCode: "uj",
    campusName: "APK Campus",
    course: "BSc Information Technology",
    yearOfStudy: 2,
    verificationStatus: "NOT_SUBMITTED",
    isStudentVerified: false,
    isAdmin: false
  }));
});

test("student cannot self-verify on profile update", async () => {
  const studentDb = testEnv.authenticatedContext("student_legit", { email: "student@uj.ac.za" }).firestore(DATABASE_ID);
  const profileRef = studentDb.collection("users").doc("student_legit");
  
  // Initial legal registration
  await profileRef.set({
    email: "student@uj.ac.za",
    displayName: "Thabo Mokoena",
    campusCode: "uj",
    verificationStatus: "NOT_SUBMITTED",
    isStudentVerified: false,
    isAdmin: false
  });

  // Trying to self-verify must FAIL
  await assertFails(profileRef.update({
    verificationStatus: "VERIFIED",
    isStudentVerified: true
  }));
});

test("student can submit document transitioning to PENDING", async () => {
  const studentDb = testEnv.authenticatedContext("student_doc", { email: "student@uct.ac.za" }).firestore(DATABASE_ID);
  const profileRef = studentDb.collection("users").doc("student_doc");
  
  await profileRef.set({
    email: "student@uct.ac.za",
    displayName: "Sipho Dlamini",
    campusCode: "uct",
    verificationStatus: "NOT_SUBMITTED",
    isStudentVerified: false,
    isAdmin: false
  });

  // Submitting proof document to transition to PENDING must succeed
  await assertSucceeds(profileRef.update({
    verificationStatus: "PENDING",
    verificationDocumentUrl: "https://storage.googleapis.com/test/proof.pdf",
    verificationDocumentName: "uct_proof_of_registration_2026.pdf"
  }));
});

test("authorised admin can approve and mark student as VERIFIED", async () => {
  // First student registers
  const studentDb = testEnv.authenticatedContext("student_applicant", { email: "applicant@up.ac.za" }).firestore(DATABASE_ID);
  const profileRef = studentDb.collection("users").doc("student_applicant");
  
  await profileRef.set({
    email: "applicant@up.ac.za",
    displayName: "Kagiso Rabada",
    campusCode: "up",
    verificationStatus: "PENDING",
    isStudentVerified: false,
    isAdmin: false
  });

  // Admin approves verification
  const adminDb = testEnv.authenticatedContext("admin_officer", {
    email: "admin@campusplug.co.za",
    admin: true
  }).firestore(DATABASE_ID);

  const adminProfileRef = adminDb.collection("users").doc("student_applicant");
  await assertSucceeds(adminProfileRef.update({
    verificationStatus: "VERIFIED",
    isStudentVerified: true
  }));
});

test("student cannot update another student's profile", async () => {
  const studentDb = testEnv.authenticatedContext("student_attacker", { email: "attacker@wits.ac.za" }).firestore(DATABASE_ID);
  const victimProfileRef = studentDb.collection("users").doc("victim_student");
  
  await assertFails(victimProfileRef.update({
    displayName: "Hacked Name"
  }));
});

test("student can create a campus community", async () => {
  const studentDb = testEnv.authenticatedContext("student_creator", { email: "creator@wits.ac.za" }).firestore(DATABASE_ID);
  const commRef = studentDb.collection("communities").doc("wits_compsci");

  await assertSucceeds(commRef.set({
    name: "Wits Computer Science & AI Club",
    category: "Academic & Tech",
    creatorId: "student_creator",
    institution: "University of the Witwatersrand",
    campusCode: "wits",
    description: "Official community for software developers and AI enthusiasts at Wits.",
    memberCount: 1,
    memberUids: ["student_creator"]
  }));
});

test("students can join and leave community updating member lists", async () => {
  const creatorDb = testEnv.authenticatedContext("student_creator", { email: "creator@wits.ac.za" }).firestore(DATABASE_ID);
  const commRef = creatorDb.collection("communities").doc("wits_compsci");

  await commRef.set({
    name: "Wits Computer Science & AI Club",
    category: "Academic & Tech",
    creatorId: "student_creator",
    campusCode: "wits",
    memberCount: 1,
    memberUids: ["student_creator"]
  });

  // Second student joins
  const joinerDb = testEnv.authenticatedContext("student_joiner", { email: "joiner@wits.ac.za" }).firestore(DATABASE_ID);
  const joinerRef = joinerDb.collection("communities").doc("wits_compsci");

  await assertSucceeds(joinerRef.update({
    memberCount: 2,
    memberUids: ["student_creator", "student_joiner"]
  }));

  // Non-creator cannot rename the community
  await assertFails(joinerRef.update({
    name: "Hacked Community Name"
  }));
});

test("student can create post in community discussion and other student can like it", async () => {
  const creatorDb = testEnv.authenticatedContext("student_creator", { email: "creator@wits.ac.za" }).firestore(DATABASE_ID);
  const postRef = creatorDb.collection("communities").doc("wits_compsci").collection("posts").doc("post123");

  await assertSucceeds(postRef.set({
    communityId: "wits_compsci",
    authorId: "student_creator",
    authorName: "Kagiso Developer",
    content: "Who is attending the upcoming campus hackathon this weekend?",
    title: "Hackathon 2026 prep",
    likesCount: 0,
    likedBy: []
  }));

  // Another student likes the post
  const peerDb = testEnv.authenticatedContext("student_peer", { email: "peer@wits.ac.za" }).firestore(DATABASE_ID);
  const peerPostRef = peerDb.collection("communities").doc("wits_compsci").collection("posts").doc("post123");

  await assertSucceeds(peerPostRef.update({
    likesCount: 1,
    likedBy: ["student_peer"]
  }));

  // Peer cannot edit the content of the author's post
  await assertFails(peerPostRef.update({
    content: "Content vandalized by peer"
  }));
});

test("student can create feed post with image attachment", async () => {
  const authorDb = testEnv.authenticatedContext("student_poster", { email: "author@uj.ac.za" }).firestore(DATABASE_ID);
  const postRef = authorDb.collection("posts").doc("feed_post_1");

  await assertSucceeds(postRef.set({
    authorId: "student_poster",
    authorName: "Kopano Sithole",
    authorCampus: "uj",
    isAuthorVerified: true,
    content: "Classes cancelled for faculty sports day tomorrow! See attached notice.",
    imageUrl: "https://storage.googleapis.com/campusplug/notice.jpg",
    likesCount: 0,
    likedBy: [],
    commentsCount: 0
  }));
});

test("student can like feed post, but cannot tamper with post content", async () => {
  const authorDb = testEnv.authenticatedContext("student_poster", { email: "author@uj.ac.za" }).firestore(DATABASE_ID);
  const postRef = authorDb.collection("posts").doc("feed_post_2");

  await postRef.set({
    authorId: "student_poster",
    authorName: "Kopano Sithole",
    content: "Studying for electrical engineering test at Doornfontein library.",
    likesCount: 0,
    likedBy: [],
    commentsCount: 0
  });

  const likerDb = testEnv.authenticatedContext("student_liker", { email: "liker@uj.ac.za" }).firestore(DATABASE_ID);
  const likerPostRef = likerDb.collection("posts").doc("feed_post_2");

  // Like succeeds
  await assertSucceeds(likerPostRef.update({
    likesCount: 1,
    likedBy: ["student_liker"]
  }));

  // Attempt to alter authorId or content fails
  await assertFails(likerPostRef.update({
    content: "Hacked post content"
  }));
});

test("author can delete their own post, non-author cannot", async () => {
  const authorDb = testEnv.authenticatedContext("student_poster", { email: "author@uj.ac.za" }).firestore(DATABASE_ID);
  const postRef = authorDb.collection("posts").doc("feed_post_del");

  await postRef.set({
    authorId: "student_poster",
    authorName: "Kopano Sithole",
    content: "Accidental test post to delete.",
    likesCount: 0,
    likedBy: [],
    commentsCount: 0
  });

  const attackerDb = testEnv.authenticatedContext("student_attacker", { email: "attacker@uj.ac.za" }).firestore(DATABASE_ID);
  const attackerRef = attackerDb.collection("posts").doc("feed_post_del");

  // Attacker cannot delete
  await assertFails(attackerRef.delete());

  // Author can delete
  await assertSucceeds(postRef.delete());
});

test("student can comment on feed post and author can delete comment", async () => {
  const authorDb = testEnv.authenticatedContext("student_poster", { email: "author@uj.ac.za" }).firestore(DATABASE_ID);
  const postRef = authorDb.collection("posts").doc("feed_post_comm");

  await postRef.set({
    authorId: "student_poster",
    authorName: "Kopano Sithole",
    content: "Who has the textbook for INF301?",
    likesCount: 0,
    likedBy: [],
    commentsCount: 0
  });

  const commenterDb = testEnv.authenticatedContext("student_commenter", { email: "commenter@uj.ac.za" }).firestore(DATABASE_ID);
  const commentRef = commenterDb.collection("posts").doc("feed_post_comm").collection("comments").doc("comment_1");

  await assertSucceeds(commentRef.set({
    postId: "feed_post_comm",
    authorId: "student_commenter",
    authorName: "Lindiwe Ndlovu",
    content: "I have the PDF! DM me on WhatsApp."
  }));

  // Commenter can delete own comment
  await assertSucceeds(commentRef.delete());
});
