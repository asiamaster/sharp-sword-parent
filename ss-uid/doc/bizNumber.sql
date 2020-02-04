CREATE TABLE `biz_number` (
	`id` BIGINT(20) NULL DEFAULT NULL,
	`type` VARCHAR(150) NULL DEFAULT NULL,
	`value` BIGINT(20) NULL DEFAULT NULL,
	`memo` VARCHAR(150) NULL DEFAULT NULL,
	`version` BIGINT(20) NULL DEFAULT NULL,
	`modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
	`created` DATETIME NULL DEFAULT NULL
)
COMMENT='业务号\r\n记录所有业务的编号\r\n如：\r\n回访编号:HF201712080001'
COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB
AUTO_INCREMENT=13
;

INSERT INTO `biz_number` (`id`, `type`, `value`, `memo`, `version`, `modified`, `created`) VALUES (1, 'order', 2019020200014001, '订单编码', '1', '2018-11-02 14:56:16', '2018-11-02 09:45:13');
INSERT INTO `biz_number` (`id`, `type`, `value`, `memo`, `version`, `modified`, `created`) VALUES (2, 'customer', 11000, '客户编码', '1', '2018-11-02 14:56:21', '2018-11-02 09:45:13');
INSERT INTO `biz_number` (`id`, `type`, `value`, `memo`, `version`, `modified`, `created`) VALUES (3, 'campaign', 2350, '活动编码', '1', '2018-11-02 14:56:22', '2018-11-02 14:09:29');
INSERT INTO `biz_number` (`id`, `type`, `value`, `memo`, `version`, `modified`, `created`) VALUES (4, 'refund_order', 2019020200000051, '退款单编码', '1', '2018-11-02 14:56:29', '2018-11-02 14:11:08');
INSERT INTO `biz_number` (`id`, `type`, `value`, `memo`, `version`, `modified`, `created`) VALUES (5, 'group_buy', 201812200251, '普通团购编码', '1', '2018-11-02 14:56:32', '2018-11-02 14:11:54');
INSERT INTO `biz_number` (`id`, `type`, `value`, `memo`, `version`, `modified`, `created`) VALUES (6, 'leader_group_buy', 201812200051, '团长团购编码', '1', '2018-11-02 14:56:34', '2018-11-02 14:12:15');
INSERT INTO `biz_number` (`id`, `type`, `value`, `memo`, `version`, `modified`, `created`) VALUES (7, 'shop', 1900, '门店编码', '1', '2018-11-02 14:56:24', '2018-11-02 14:12:34');
INSERT INTO `biz_number` (`id`, `type`, `value`, `memo`, `version`, `modified`, `created`) VALUES (8, 'product', 2019020200000101, '商品编码', '1', '2018-11-02 14:56:36', '2018-11-02 14:13:00');
INSERT INTO `biz_number` (`id`, `type`, `value`, `memo`, `version`, `modified`, `created`) VALUES (9, 'coupon', 451, '优惠券编码', '1', '2019-01-09 15:02:06', '2019-01-09 15:02:06');
INSERT INTO `biz_number` (`id`, `type`, `value`, `memo`, `version`, `modified`, `created`) VALUES (10, 'warehouse', 501, '仓库编码', '1', '2019-01-09 15:02:16', '2019-01-09 15:02:16');
INSERT INTO `biz_number` (`id`, `type`, `value`, `memo`, `version`, `modified`, `created`) VALUES (11, 'logistics_order', 2019020200000051, '物流单编码', '1', '2019-01-16 14:53:02', '2019-01-16 14:53:02');
INSERT INTO `biz_number` (`id`, `type`, `value`, `memo`, `version`, `modified`, `created`) VALUES (12, 'serial', 2019013000000051, '余额交易流水号', '1', '2019-01-29 19:20:39', '2019-01-29 19:20:26');
