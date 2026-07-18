/*
 Navicat Premium Data Transfer

 Source Server         : localhost-mongo
 Source Server Type    : MongoDB
 Source Server Version : 70011 (7.0.11)
 Source Host           : localhost:27017
 Source Schema         : iwindplus-log

 Target Server Type    : MongoDB
 Target Server Version : 70011 (7.0.11)
 File Encoding         : 65001

 Date: 23/02/2025 16:01:48
*/


// ----------------------------
// Collection structure for gateway_log
// ----------------------------
db.getCollection("gateway_log").drop();
db.createCollection("gateway_log");
db.getCollection("gateway_log").createIndex({
    target_server: NumberInt("1")
}, {
    name: "idx_target_server"
});
db.getCollection("gateway_log").createIndex({
    trace_id: NumberInt("1")
}, {
    name: "idx_trace_id"
});
db.getCollection("gateway_log").createIndex({
    user_id: NumberInt("1")
}, {
    name: "idx_user_id"
});
db.getCollection("gateway_log").createIndex({
    org_id: NumberInt("1")
}, {
    name: "idx_org_id"
});

// ----------------------------
// Collection structure for login_log
// ----------------------------
db.getCollection("login_log").drop();
db.createCollection("login_log");
db.getCollection("login_log").createIndex({
    trace_id: NumberInt("1")
}, {
    name: "idx_trace_id"
});
db.getCollection("login_log").createIndex({
    user_id: NumberInt("1")
}, {
    name: "idx_user_id"
});
db.getCollection("login_log").createIndex({
    org_id: NumberInt("1")
}, {
    name: "idx_org_id"
});

// ----------------------------
// Collection structure for mail_captcha_log
// ----------------------------
db.getCollection("mail_captcha_log").drop();
db.createCollection("mail_captcha_log");
db.getCollection("mail_captcha_log").createIndex({
    mail: NumberInt("1")
}, {
    name: "idx_mail"
});
db.getCollection("mail_captcha_log").createIndex({
    captcha: NumberInt("1")
}, {
    name: "idx_captcha"
});

// ----------------------------
// Collection structure for mail_log
// ----------------------------
db.getCollection("mail_log").drop();
db.createCollection("mail_log");
db.getCollection("mail_log").createIndex({
    subject: NumberInt("1")
}, {
    name: "idx_subject"
});
db.getCollection("mail_log").createIndex({
    username: NumberInt("1")
}, {
    name: "idx_username"
});
db.getCollection("mail_log").createIndex({
    nick_name: NumberInt("1")
}, {
    name: "idx_nickName"
});

// ----------------------------
// Collection structure for operation_log
// ----------------------------
db.getCollection("operation_log").drop();
db.createCollection("operation_log");
db.getCollection("operation_log").createIndex({
    target_server: NumberInt("1")
}, {
    name: "idx_target_server"
});
db.getCollection("operation_log").createIndex({
    biz_number: NumberInt("1")
}, {
    name: "idx_biz_number"
});
db.getCollection("operation_log").createIndex({
    trace_id: NumberInt("1")
}, {
    name: "idx_trace_id"
});
db.getCollection("operation_log").createIndex({
    ip: NumberInt("1")
}, {
    name: "idx_ip"
});
db.getCollection("operation_log").createIndex({
    user_id: NumberInt("1")
}, {
    name: "idx_user_id"
});
db.getCollection("operation_log").createIndex({
    org_id: NumberInt("1")
}, {
    name: "idx_org_id"
});

// ----------------------------
// Collection structure for sms_captcha_log
// ----------------------------
db.getCollection("sms_captcha_log").drop();
db.createCollection("sms_captcha_log");
db.getCollection("sms_captcha_log").createIndex({
    biz_number: NumberInt("1")
}, {
    name: "idx_biz_number"
});
db.getCollection("sms_captcha_log").createIndex({
    mobile: NumberInt("1")
}, {
    name: "idx_mobile"
});
db.getCollection("sms_captcha_log").createIndex({
    captcha: NumberInt("1")
}, {
    name: "idx_captcha"
});
