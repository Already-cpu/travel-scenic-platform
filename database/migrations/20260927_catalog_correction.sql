-- Correct only the known legacy demo catalog; keep IDs and all user-created records.
-- First make a backup; safe to repeat. No tables or accounts are deleted.
USE travel_website;
SET NAMES utf8mb4;
START TRANSACTION;
UPDATE scenic SET name='桂林漓江',province='广西壮族自治区',city='桂林市',address='桂林市漓江景区',type='湖泊山水',price='180',intro='喀斯特峰林与江水相伴，可沿江欣赏层叠山峰和田园风光。',image='/scenic-images/11-li-river.jpg',open_time='08:00-18:00（演示时间，以官方为准）' WHERE id=11 AND name='多洛米蒂高山湖' AND image='/scenic-images/11-lijiang.jpg';
UPDATE scenic SET name='九寨沟',province='四川省',city='阿坝州',address='九寨沟县漳扎镇',type='自然风光',price='190',intro='沿栈道游览五花海等高山湖泊，欣赏彩色湖水与森林相映的山谷景观。',image='/scenic-images/12-jiuzhaigou.jpg',open_time='08:00-18:00（演示时间，以官方为准）' WHERE id=12 AND name='九寨沟山水' AND image='/scenic-images/12-jiuzhaigou.jpg';
UPDATE scenic SET name='喀纳斯',province='新疆维吾尔自治区',city='阿勒泰地区',address='布尔津县喀纳斯景区',type='湖泊山水',price='160',intro='湖泊、河湾与阿尔泰山林构成北疆风景，适合沿观景步道慢游。',image='/scenic-images/13-kanas.jpg',open_time='08:00-18:00（演示时间，以官方为准）' WHERE id=13 AND name='雪山星空营地' AND image='/scenic-images/13-changbaishan.jpg';
UPDATE scenic SET name='大理洱海',province='云南省',city='大理市',address='大理市洱海湖区',type='湖泊山水',price='0',intro='沿环湖步道欣赏苍山与湖水，感受大理村落的生活气息。',image='/scenic-images/14-erhai.jpg',open_time='08:00-18:00（演示时间，以官方为准）' WHERE id=14 AND name='苍山洱海' AND image='/scenic-images/14-erhai-lake.jpg';
UPDATE scenic SET name='布达拉宫',province='西藏自治区',city='拉萨市',address='拉萨市城关区北京中路',type='历史古迹',price='200',intro='依山而建的宫堡建筑群，是了解当地建筑与历史文化的重要窗口。',image='/scenic-images/15-potala.jpg',open_time='08:00-18:00（演示时间，以官方为准）' WHERE id=15 AND name='高原山谷' AND image='/scenic-images/15-qinghai-lake.jpg';
UPDATE scenic SET name='云台山',province='河南省',city='焦作市',address='修武县云台山景区',type='山岳风光',price='120',intro='峡谷、岩壁、溪流与瀑布相互映衬，可沿山间步道欣赏地质景观。',image='/scenic-images/16-yuntai.jpg',open_time='08:00-18:00（演示时间，以官方为准）' WHERE id=16 AND name='云台山瀑布' AND image='/scenic-images/16-waterfall.jpg';
UPDATE scenic SET name='玉龙雪山',province='云南省',city='丽江市',address='玉龙纳西族自治县玉龙雪山景区',type='山岳风光',price='100',intro='雪峰与山地草甸层次分明，游览前应留意天气和高海拔环境。',image='/scenic-images/17-yulong.jpg',open_time='08:00-18:00（演示时间，以官方为准）' WHERE id=17 AND name='镜湖雪山' AND image='/scenic-images/17-mountain-lake.jpg';
UPDATE scenic SET name='千岛湖',province='浙江省',city='杭州市',address='淳安县千岛湖镇',type='湖泊山水',price='130',intro='湖面岛屿散布，可乘游船或沿湖步道欣赏岛屿和远山。',image='/scenic-images/18-qiandao.jpg',open_time='08:00-18:00（演示时间，以官方为准）' WHERE id=18 AND name='森林秘境' AND image='/scenic-images/18-forest.jpg';
UPDATE scenic SET name='张掖七彩丹霞',province='甘肃省',city='张掖市',address='临泽县七彩丹霞景区',type='自然风光',price='54',intro='不同色彩的山体条带起伏相连，观景台可欣赏丹霞地貌的整体层次。',image='/scenic-images/19-danxia.jpg',open_time='08:00-18:00（演示时间，以官方为准）' WHERE id=19 AND name='丹霞峡谷' AND image='/scenic-images/19-mountain-valley.jpg';
COMMIT;
