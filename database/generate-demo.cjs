/* Regenerates public demo SQL only. Never reads or exports the live database.
 * Run: node database/generate-demo.cjs
 * Output: seed_demo.sql + travel_website.sql. Import is additive and idempotent.
 */
const fs = require('fs'), path = require('path');
const root = __dirname;
const scenic = [...JSON.parse(fs.readFileSync(path.join(root,'base-scenic.json'),'utf8')), ...JSON.parse(fs.readFileSync(path.join(root,'scenic-catalog.json'),'utf8'))];
const q = value => "'" + String(value).replaceAll("\\","\\\\").replaceAll("'","''") + "'";
const hash = '$2a$10$T6gy6yotOWfgA4/aGZv6n.7z0sT0VehMkXJoQE8muh2zFcKQj9FN2';
const adminHash = '$2a$10$Vm1De0qUNhYyVOP2zQzouutp/WGwWu0l/qM5DPr47NRoSoZlVDUFO';
const phone = n => '19900001' + String(n).padStart(3,'0');
const statements = [
  '-- PUBLIC DEMONSTRATION DATA ONLY. Prices, orders, reviews and view counts are synthetic.',
  '-- Safe to repeat: no DROP/TRUNCATE/DELETE, no overwriting existing accounts or orders.',
  'USE travel_website;', 'SET NAMES utf8mb4;', 'START TRANSACTION;',
  'SET @demo_today = CURDATE();',
  "SET @demo_start = DATE_SUB(DATE_SUB(@demo_today, INTERVAL DAYOFMONTH(@demo_today)-1 DAY), INTERVAL 11 MONTH);",
  "INSERT INTO admin (username,password,nickname) SELECT 'admin'," + q(adminHash) + ",'平台管理员' WHERE NOT EXISTS(SELECT 1 FROM admin WHERE username='admin');",
  "INSERT INTO user (phone,password,nickname,status) SELECT '13800138000'," + q(hash) + ",'演示旅行者',1 WHERE NOT EXISTS(SELECT 1 FROM user WHERE phone='13800138000');"
];
for (const s of scenic) {
  const fields = ['name','province','city','address','type','price','intro','image','open_time','views','status'];
  const values = [s.name,s.province,s.city,s.address,s.type,s.price,s.intro,s.image,s.open_time || '08:00-18:00（演示时间，以官方为准）',s.views || 4300+s.id*317,1];
  statements.push('INSERT INTO scenic ('+fields.join(',')+') SELECT '+values.map(v=>typeof v==='number'?v:q(v)).join(',')+' WHERE NOT EXISTS(SELECT 1 FROM scenic WHERE name='+q(s.name)+' OR image='+q(s.image)+');');
  statements.push('SET @scenic_'+s.id+'=(SELECT id FROM scenic WHERE name='+q(s.name)+' ORDER BY id LIMIT 1);');
}
const names=['山野','小满','清风','远方','南风','拾光','晴川','木棉','听雨','云舟','青禾','溪行'];
for(let u=1;u<=60;u++) {
  const month=Math.floor((u-1)/5);
  statements.push('INSERT INTO user (phone,password,nickname,status,create_time,update_time,last_login_time) SELECT '+[
    q(phone(u)),q(hash),q('演示·'+names[(u-1)%12]+String(u).padStart(2,'0')),1,
    'DATE_ADD(@demo_start, INTERVAL '+month+' MONTH)','NOW()',
    u%10===0?'NULL':u%8===0?'@demo_today':'GREATEST(DATE_ADD(@demo_start, INTERVAL '+month+' MONTH), DATE_SUB(@demo_today, INTERVAL '+(u%40)+' DAY))'
  ].join(',')+' WHERE NOT EXISTS(SELECT 1 FROM user WHERE phone='+q(phone(u))+');');
  statements.push('SET @user_'+u+'=(SELECT id FROM user WHERE phone='+q(phone(u))+" AND nickname LIKE '演示·%' LIMIT 1);");
}
const aspects={
  '湖泊山水':['湖面倒影','沿湖步道','水面与远山'],
  '历史古迹':['古建筑细节','参观动线','讲解内容'],
  '自然风光':['山谷层次','栈道沿途','观景台视野'],
  '海滨休闲':['海边风景','岛上街巷','步行路线'],
  '山岳风光':['山间步道','远山层次','登高视野'],
  '亲子研学':['动物科普','参观秩序','亲子体验'],
  '博物馆':['展陈内容','历史讲解','文物细节'],
  '古镇人文':['水巷石桥','古镇街巷','沿河风景'],
  '海岛度假':['沙滩环境','海岸风景','步行体验'],
  '主题乐园':['游乐项目','园区氛围','主题演出']
};
const comments={
  1:['排队和等候时间太长，整体体验不太理想，希望增加引导。','休息点不够好找，这次安排得比较仓促，体验低于预期。'],
  2:['沿途标识还可以更清楚，走了不少回头路，建议优化路线说明。','人多时体验一般，休息区域比较紧张，建议错峰安排。'],
  3:['整体中规中矩，建议预留更充裕的时间，先看路线再出发。','景观值得一看，但游览节奏有点赶，下次会安排半天慢慢走。'],
  4:['沿路走走停停很舒服，提前规划路线后体验更好，建议穿舒适的鞋。','观景和拍照都不错，部分路段人稍多，早一点到会更从容。'],
  5:['现场比照片更有层次，慢慢逛很有收获，愿意再安排一次旅行。','整体体验很满意，路线容易理解，一家人都留下了不错的印象。']
};
function addOrder(key,u,s,quantity,status,created,visit) {
  statements.push('INSERT INTO `order` (order_no,user_id,scenic_id,scenic_name,visit_date,ticket_type,quantity,total_price,contact_name,contact_phone,status,create_time,update_time) SELECT '+[
    q(key),'u.id','s.id','s.name',visit,q('成人票'),quantity,'s.price*'+quantity,'u.nickname','u.phone',q(status),created,
    status==='COMPLETED'?'TIMESTAMP('+visit+")":created
  ].join(',')+' FROM user u JOIN scenic s ON s.id=@scenic_'+s.id+' WHERE u.id=@user_'+u+' AND NOT EXISTS(SELECT 1 FROM `order` WHERE order_no='+q(key)+');');
}
let orders=0, reviews=0, favorites=0;
for (const s of scenic) {
  const reviewers = new Set();
  for(let j=0;j<10;j++) {
    const m=j+1;
    let u=1+(s.id*7+j*3)%((m+1)*5);
    while (reviewers.has(u)) u = u % ((m+1)*5) + 1;
    reviewers.add(u);
    const created='DATE_ADD(DATE_ADD(@demo_start, INTERVAL '+m+' MONTH), INTERVAL '+(2+(s.id+j)%18)+' DAY)';
    const visit='DATE_ADD('+created+', INTERVAL 2 DAY)';
    const key='DEMO_V2_REVIEW_'+String(s.id).padStart(2,'0')+'_'+j;
    addOrder(key,u,s,1+(s.id+j)%3,'COMPLETED',created,visit);orders++;
    const scoreSets=[[1,2,3,3,4,4,4,5,5,5],[2,3,4,4,4,4,5,5,5,5],[1,3,3,4,4,5,5,5,5,5],[2,2,3,4,4,4,4,5,5,5]];
    const score=scoreSets[s.id%4][j],aspect=(aspects[s.type]||['沿途风景','游览路线','景观细节'])[j%3];
    const content='【演示评价】'+s.name+'的'+aspect+'让人印象深刻。'+comments[score][(s.id+j)%2];
    const reply=j%4===0?q('【演示回复】感谢反馈，我们会持续完善路线提示与服务。'):'NULL';
    statements.push('INSERT INTO comment (user_id,scenic_id,score,content,reply,status,create_time) SELECT o.user_id,o.scenic_id,'+score+','+q(content)+','+reply+",1,DATE_ADD(o.visit_date,INTERVAL 1 DAY) FROM `order` o WHERE o.order_no="+q(key)+' AND NOT EXISTS(SELECT 1 FROM comment c WHERE c.user_id=o.user_id AND c.scenic_id=o.scenic_id);');reviews++;
  }
  for(let j=0;j<12;j++){
    const u=1+(s.id*13+j*3)%60;
    statements.push('INSERT INTO favorite (user_id,scenic_id,create_time) SELECT @user_'+u+',@scenic_'+s.id+',@demo_today WHERE @user_'+u+' IS NOT NULL AND @scenic_'+s.id+' IS NOT NULL AND NOT EXISTS(SELECT 1 FROM favorite WHERE user_id=@user_'+u+' AND scenic_id=@scenic_'+s.id+');');favorites++;
  }
}
for(let m=0;m<12;m++){
  for(let j=0;j<12+m*2;j++){
    const s=scenic[(m*7+j*5)%scenic.length], u=1+(m*11+j)%((m+1)*5);
    const created='LEAST(DATE_ADD(DATE_ADD(@demo_start, INTERVAL '+m+' MONTH), INTERVAL '+(j%23)+' DAY),@demo_today)';
    const status=['COMPLETED','COMPLETED','PAID','CANCELLED','PENDING'][j%5];
    const visit=status==='COMPLETED'?'LEAST(DATE_ADD('+created+',INTERVAL 3 DAY),@demo_today)':'DATE_ADD('+created+',INTERVAL '+(m===11?14:4)+' DAY)';
    addOrder('DEMO_V2_MONTH_'+String(m).padStart(2,'0')+'_'+String(j).padStart(2,'0'),u,s,1+j%4,status,created,visit);orders++;
  }
}
statements.push('COMMIT;');
const seed=statements.join('\n')+'\n';
fs.writeFileSync(path.join(root,'seed_demo.sql'),seed);
fs.writeFileSync(path.join(root,'travel_website.sql'),fs.readFileSync(path.join(root,'schema.sql'),'utf8')+'\n\n'+seed);
console.log(JSON.stringify({scenic:scenic.length,newDemoUsers:60,orders,reviews,favorites},null,2));
