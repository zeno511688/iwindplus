/*
 Navicat Premium Data Transfer

 Source Server         : localhost-mongo
 Source Server Type    : MongoDB
 Source Server Version : 70011 (7.0.11)
 Source Host           : localhost:27017
 Source Schema         : iwindplus-im

 Target Server Type    : MongoDB
 Target Server Version : 70011 (7.0.11)
 File Encoding         : 65001

 Date: 23/02/2025 16:01:03
*/


// ----------------------------
// Collection structure for add_friend_msg
// ----------------------------
db.getCollection("add_friend_msg").drop();
db.createCollection("add_friend_msg");
db.getCollection("add_friend_msg").createIndex({
    receiver_id: NumberInt("1")
}, {
    name: "idx_receiver_id"
});
db.getCollection("add_friend_msg").createIndex({
    sender_id: NumberInt("1")
}, {
    name: "idx_sender_id"
});
db.getCollection("add_friend_msg").createIndex({
    org_id: NumberInt("1")
}, {
    name: "idx_org_id"
});

// ----------------------------
// Collection structure for direct_msg
// ----------------------------
db.getCollection("direct_msg").drop();
db.createCollection("direct_msg");
db.getCollection("direct_msg").createIndex({
    title: NumberInt("1")
}, {
    name: "idx_title"
});
db.getCollection("direct_msg").createIndex({
    receiver_id: NumberInt("1")
}, {
    name: "idx_receiver_id"
});
db.getCollection("direct_msg").createIndex({
    sender_id: NumberInt("1")
}, {
    name: "idx_sender_id"
});
db.getCollection("direct_msg").createIndex({
    org_id: NumberInt("1")
}, {
    name: "idx_org_id"
});

// ----------------------------
// Collection structure for friend_chat_msg
// ----------------------------
db.getCollection("friend_chat_msg").drop();
db.createCollection("friend_chat_msg");
db.getCollection("friend_chat_msg").createIndex({
    receiver_id: NumberInt("1")
}, {
    name: "idx_receiver_id"
});
db.getCollection("friend_chat_msg").createIndex({
    sender_id: NumberInt("1")
}, {
    name: "idx_sender_id"
});
db.getCollection("friend_chat_msg").createIndex({
    org_id: NumberInt("1")
}, {
    name: "idx_org_id"
});

// ----------------------------
// Collection structure for group_chat_msg
// ----------------------------
db.getCollection("group_chat_msg").drop();
db.createCollection("group_chat_msg");
db.getCollection("group_chat_msg").createIndex({
    chat_group_id: NumberInt("1")
}, {
    name: "idx_chat_group_id"
});
db.getCollection("group_chat_msg").createIndex({
    sender_id: NumberInt("1")
}, {
    name: "idx_sender_id"
});
db.getCollection("group_chat_msg").createIndex({
    org_id: NumberInt("1")
}, {
    name: "idx_org_id"
});

// ----------------------------
// Collection structure for sys_notice_msg
// ----------------------------
db.getCollection("sys_notice_msg").drop();
db.createCollection("sys_notice_msg");
db.getCollection("sys_notice_msg").createIndex({
    title: NumberInt("1")
}, {
    name: "idx_title"
});
db.getCollection("sys_notice_msg").createIndex({
    sender_id: NumberInt("1")
}, {
    name: "idx_sender_id"
});
db.getCollection("sys_notice_msg").createIndex({
    org_id: NumberInt("1")
}, {
    name: "idx_org_id"
});
