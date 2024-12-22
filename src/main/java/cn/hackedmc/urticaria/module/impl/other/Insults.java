package cn.hackedmc.urticaria.module.impl.other;

import cn.hackedmc.urticaria.Client;
import cn.hackedmc.urticaria.api.Rise;
import cn.hackedmc.urticaria.component.impl.player.BadPacketsComponent;
import cn.hackedmc.urticaria.module.Module;
import cn.hackedmc.urticaria.module.api.Category;
import cn.hackedmc.urticaria.module.api.ModuleInfo;
import cn.hackedmc.urticaria.module.impl.combat.KillAura;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.input.ChatInputEvent;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.newevent.impl.other.AttackEvent;
import cn.hackedmc.urticaria.newevent.impl.other.WorldChangeEvent;
import cn.hackedmc.urticaria.newevent.impl.packet.PacketReceiveEvent;
import cn.hackedmc.urticaria.util.RandomUtil;
import cn.hackedmc.urticaria.util.player.PlayerUtil;
import cn.hackedmc.urticaria.value.impl.BooleanValue;
import cn.hackedmc.urticaria.value.impl.ModeValue;
import cn.hackedmc.urticaria.value.impl.NumberValue;
import cn.hackedmc.urticaria.value.impl.SubMode;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S02PacketChat;
import org.apache.commons.lang3.RandomUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Rise
@ModuleInfo(name = "module.other.insults.name", description = "module.other.insults.description", category = Category.OTHER)
public final class Insults extends Module {

    public final ModeValue mode = new ModeValue("Mode", this)
            .add(new SubMode("Default"))
            .add(new SubMode("Watchdog"))
            .add(new SubMode("WhatsApp"))
            .add(new SubMode("Urticaria"))
            .setDefault("Default");

    private final BooleanValue faDian = new BooleanValue("Fa Dian", this, false);
    private final BooleanValue publicChat = new BooleanValue("Public Chat", this, false, () -> !mode.getValue().getName().equalsIgnoreCase("urticaria"));
    public final Map<String, List<String>> map = new HashMap<>();

    private final NumberValue delay = new NumberValue("Delay", this, 0, 0, 50, 1);

    private final String[] lyrics = {
            "再牛b的肖邦也弹不出劳资的悲伤",
            "我不需要伪装 你心情不好读点鸡汤",
            "我要变成心脏 下辈子我变成你心脏",
            "我不跳你就会死 这不可替代的位置",
            "再牛b的肖邦也弹不出劳资的悲伤",
            "我不需要伪装 你心情不好读点鸡汤",
            "我要变成心脏 下辈子我变成你心脏",
            "我不跳你就会死 这不可替代的位置",
            "666 我新换了QQ秀",
            "QQ空间农场里面养了一只大狼狗",
            "oh my girl 我说声i 服了u",
            "相册里面太多伤感大图 我勒个豆",
            "我的键盘从来没空格键",
            "我在劲舞团里跳娃娃脸",
            "边跳边聊天",
            "左边头发它遮挡我右眼",
            "我在网络上面装着抑郁",
            "悲伤一整天",
            "45度角仰望着星空",
            "你的聊天框再没有叮咚",
            "再牛逼的肖邦也弹不出劳资的悲伤",
            "我不需要伪装 你心情不好读点鸡汤",
            "我要变成心脏 下辈子我变成你心脏",
            "我不跳你就会死 这不可替代的位置",
            "再牛逼的肖邦也弹不出劳资的悲伤",
            "我不需要伪装 你心情不好读点鸡汤",
            "我要变成心脏 下辈子我变成你心脏",
            "我不跳你就会死 这不可替代的位置",
            "烟花烫 我忧郁中带狂",
            "棉花糖 你说我唱太娘",
            "若有人动你翅膀 我毁了他天堂",
            "劳资是真的爱你 你飞向别人的床",
            "龍少哭了求点赞",
            "腾讯QB发不完",
            "转发Q群升皇冠",
            "网络姻缘情难断",
            "飞轮海为我解散",
            "徐良成为我的fans",
            "我是感情中的骗子",
            "我是芳心纵火犯",
            "45度角仰望着星空",
            "你的聊天框再没有叮咚",
            "再牛逼的肖邦也弹不出劳资的悲伤",
            "我不需要伪装 你心情不好读点鸡汤",
            "我要变成心脏 下辈子我变成你心脏",
            "我不跳你就会死 这不可替代的位置",
            "再牛逼的肖邦也弹不出劳资的悲伤",
            "我不需要伪装 你心情不好读点鸡汤",
            "我要变成心脏 下辈子我变成你心脏",
            "我不跳你就会死 这不可替代的位置",
            "耶！这依然是你最爱的MC龍少，我已经忘了我的QQ号~"
    };
    private final String[] defaultInsults = {
            "你已被Urticarial击杀，B站搜索_moran_，不见不散！",
            "爱来自美丽的中国北京狂笑的蛇将写散文，Welcome dm me.",
            "Native客户端已经称霸花雨庭，我GM即将携带我的GodMode回归。",
            "你好我叫欣欣，真名许锦良，我的身份证号码是371481200603016011，骂我你就死全家",
            "你好我是欣欣的父亲，真名许华禄，我的身份证号码是372402197211106334，骂我儿子我就杀你们全家",
            "你好我是欣欣的姐姐，真名许晓宇，我的身份证号码是371481200412106022，骂我弟弟你们死全家",
            "你好我是欣欣的爷爷，真名许炳彩，我的身份证号码是372427194409096374，骂我的乖孙子我就操死你们所有人的妈",
            "你好我是欣欣的邻居，真名邢紀钢，我的身份证号码是120112195611061314，欣欣天天半夜狗叫，请你将他妈杀",
            "你好我是欣欣的大姑，真名许立红，我的身份证号码是371481197112036021，欣欣和我就像亲儿子一样亲，我不许你们欺负他",
            "你好我是欣欣的二姑，真名许新红，我的身份证号码是372402197603306327，欣欣就他妈纯傻逼，不知道为什么我姐姐喜欢他",
            "别生气，玩游戏不是为了娱乐吗？",
            "一群傻逼，殴打得过你默然大神写的荨麻疹大端妈？",
            "操死你妈逼，偷刀狗祖宗十八代全他妈逼的死一遍",
            "傻逼玩意，是不是不敢直面你大跌我"
    };

    public final BooleanValue chatBypass = new BooleanValue("Chat Bypass", this, false);
    private final ModeValue bypassMode = new ModeValue("Mode", this, () -> !chatBypass.getValue())
            .add(new SubMode("Illegal Chat"))
            .add(new SubMode("Double Check"))
            .setDefault("Illegal Chat");

    private final String[] whatsAppInsults = {
            "Add me on WhatsApp ",
    };
    private static final String  normalChinese = "一乙二十丁厂七卜人入八九几儿了力乃刀又三于干亏士工土才寸下大丈与万上小口巾山千乞川亿个勺久凡及夕丸么广亡门义之尸弓己已子卫也女飞刃习叉马乡丰王井开夫天无元专云扎艺木五支厅不太犬区历尤友匹车巨牙屯比互切瓦止少日中冈贝内水见午牛手毛气升长仁什片仆化仇币仍仅斤爪反介父从今凶分乏公仓月氏勿欠风丹匀乌凤勾文六方火为斗忆订计户认心尺引丑巴孔队办以允予劝双书幻玉刊示末未击打巧正扑扒功扔去甘世古节本术可丙左厉右石布龙平灭轧东卡北占业旧帅归且旦目叶甲申叮电号田由史只央兄叼叫另叨叹四生失禾丘付仗代仙们仪白仔他斥瓜乎丛令用甩印乐句匆册犯外处冬鸟务包饥主市立闪兰半汁汇头汉宁穴它讨写让礼训必议讯记永司尼民出辽奶奴加召皮边发孕圣对台矛纠母幼丝式刑动扛寺吉扣考托老执巩圾扩扫地扬场耳共芒亚芝朽朴机权过臣再协西压厌在有百存而页匠夸夺灰达列死成夹轨邪划迈毕至此贞师尘尖劣光当早吐吓虫曲团同吊吃因吸吗屿帆岁回岂刚则肉网年朱先丢舌竹迁乔伟传乒乓休伍伏优伐延件任伤价份华仰仿伙伪自血向似后行舟全会杀合兆企众爷伞创肌朵杂危旬旨负各名多争色壮冲冰庄庆亦刘齐交次衣产决充妄闭问闯羊并关米灯州汗污江池汤忙兴宇守宅字安讲军许论农讽设访寻那迅尽导异孙阵阳收阶阴防奸如妇好她妈戏羽观欢买红纤级约纪驰巡寿弄麦形进戒吞远违运扶抚坛技坏扰拒找批扯址走抄坝贡攻赤折抓扮抢孝均抛投坟抗坑坊抖护壳志扭块声把报却劫芽花芹芬苍芳严芦劳克苏杆杠杜材村杏极李杨求更束豆两丽医辰励否还歼来连步坚旱盯呈时吴助县里呆园旷围呀吨足邮男困吵串员听吩吹呜吧吼别岗帐财针钉告我乱利秃秀私每兵估体何但伸作伯伶佣低你住位伴身皂佛近彻役返余希坐谷妥含邻岔肝肚肠龟免狂犹角删条卵岛迎饭饮系言冻状亩况床库疗应冷这序辛弃冶忘闲间闷判灶灿弟汪沙汽沃泛沟没沈沉怀忧快完宋宏牢究穷灾良证启评补初社识诉诊词译君灵即层尿尾迟局改张忌际陆阿陈阻附妙妖妨努忍劲鸡驱纯纱纳纲驳纵纷纸纹纺驴纽奉玩环武青责现表规抹拢拔拣担坦押抽拐拖拍者顶拆拥抵拘势抱垃拉拦拌幸招坡披拨择抬其取苦若茂苹苗英范直茄茎茅林枝杯柜析板松枪构杰述枕丧或画卧事刺枣雨卖矿码厕奔奇奋态欧垄妻轰顷转斩轮软到非叔肯齿些虎虏肾贤尚旺具果味昆国昌畅明易昂典固忠咐呼鸣咏呢岸岩帖罗帜岭凯败贩购图钓制知垂牧物乖刮秆和季委佳侍供使例版侄侦侧凭侨佩货依的迫质欣征往爬彼径所舍金命斧爸采受乳贪念贫肤肺肢肿胀朋股肥服胁周昏鱼兔狐忽狗备饰饱饲变京享店夜庙府底剂郊废净盲放刻育闸闹郑券卷单炒炊炕炎炉沫浅法泄河沾泪油泊沿泡注泻泳泥沸波泼泽治怖性怕怜怪学宝宗定宜审宙官空帘实试郎诗肩房诚衬衫视话诞询该详建肃录隶居届刷屈弦承孟孤陕降限妹姑姐姓始驾参艰线练组细驶织终驻驼绍经贯奏春帮珍玻毒型挂封持项垮挎城挠政赴赵挡挺括拴拾挑指垫挣挤拼挖按挥挪某甚革荐巷带草茧茶荒茫荡荣故胡南药标枯柄栋相查柏柳柱柿栏树要咸威歪研砖厘厚砌砍面耐耍牵残殃轻鸦皆背战点临览竖省削尝是盼眨哄显哑冒映星昨畏趴胃贵界虹虾蚁思蚂虽品咽骂哗咱响哈咬咳哪炭峡罚贱贴骨钞钟钢钥钩卸缸拜看矩怎牲选适秒香种秋科重复竿段便俩贷顺修保促侮俭俗俘信皇泉鬼侵追俊盾待律很须叙剑逃食盆胆胜胞胖脉勉狭狮独狡狱狠贸怨急饶蚀饺饼弯将奖哀亭亮度迹庭疮疯疫疤姿亲音帝施闻阀阁差养美姜叛送类迷前首逆总炼炸炮烂剃洁洪洒浇浊洞测洗活派洽染济洋洲浑浓津恒恢恰恼恨举觉宣室宫宪突穿窃客冠语扁袄祖神祝误诱说诵垦退既屋昼费陡眉孩除险院娃姥姨姻娇怒架贺盈勇怠柔垒绑绒结绕骄绘给络骆绝绞统耕耗艳泰珠班素蚕顽盏匪捞栽捕振载赶起盐捎捏埋捉捆捐损都哲逝捡换挽热恐壶挨耻耽恭莲莫荷获晋恶真框桂档桐株桥桃格校核样根索哥速逗栗配翅辱唇夏础破原套逐烈殊顾轿较顿毙致柴桌虑监紧党晒眠晓鸭晃晌晕蚊哨哭恩唤啊唉罢峰圆贼贿钱钳钻铁铃铅缺氧特牺造乘敌秤租积秧秩称秘透笔笑笋债借值倚倾倒倘俱倡候俯倍倦健臭射躬息徒徐舰舱般航途拿爹爱颂翁脆脂胸胳脏胶脑狸狼逢留皱饿恋桨浆衰高席准座脊症病疾疼疲效离唐资凉站剖竞部旁旅畜阅羞瓶拳粉料益兼烤烘烦烧烛烟递涛浙涝酒涉消浩海涂浴浮流润浪浸涨烫涌悟悄悔悦害宽家宵宴宾窄容宰案请朗诸读扇袜袖袍被祥课谁调冤谅谈谊剥恳展剧屑弱陵陶陷陪娱娘通能难预桑绢绣验继球理捧堵描域掩捷排掉堆推掀授教掏掠培接控探据掘职基著勒黄萌萝菌菜萄菊萍菠营械梦梢梅检梳梯桶救副票戚爽聋袭盛雪辅辆虚雀堂常匙晨睁眯眼悬野啦晚啄距跃略蛇累唱患唯崖崭崇圈铜铲银甜梨犁移笨笼笛符第敏做袋悠偿偶偷您售停偏假得衔盘船斜盒鸽悉欲彩领脚脖脸脱象够猜猪猎猫猛馅馆凑减毫麻痒痕廊康庸鹿盗章竟商族旋望率着盖粘粗粒断剪兽清添淋淹渠渐混渔淘液淡深婆梁渗情惜惭悼惧惕惊惨惯寇寄宿窑密谋谎祸谜逮敢屠弹随蛋隆隐婚婶颈绩绪续骑绳维绵绸绿琴斑替款堪搭塔越趁趋超提堤博揭喜插揪搜煮援裁搁搂搅握揉斯期欺联散惹葬葛董葡敬葱落朝辜葵棒棋植森椅椒棵棍棉棚棕惠惑逼厨厦硬确雁殖裂雄暂雅辈悲紫辉敞赏掌晴暑最量喷晶喇遇喊景践跌跑遗蛙蛛蜓喝喂喘喉幅帽赌赔黑铸铺链销锁锄锅锈锋锐短智毯鹅剩稍程稀税筐等筑策筛筒答筋筝傲傅牌堡集焦傍储奥街惩御循艇舒番释禽腊脾腔鲁猾猴然馋装蛮就痛童阔善羡普粪尊道曾焰港湖渣湿温渴滑湾渡游滋溉愤慌惰愧愉慨割寒富窜窝窗遍裕裤裙谢谣谦属屡强粥疏隔隙絮嫂登缎缓编骗缘瑞魂肆摄摸填搏塌鼓摆携搬摇搞塘摊蒜勤鹊蓝墓幕蓬蓄蒙蒸献禁楚想槐榆楼概赖酬感碍碑碎碰碗碌雷零雾雹输督龄鉴睛睡睬鄙愚暖盟歇暗照跨跳跪路跟遣蛾蜂嗓置罪罩错锡锣锤锦键锯矮辞稠愁筹签简毁舅鼠催傻像躲微愈遥腰腥腹腾腿触解酱痰廉新韵意粮数煎塑慈煤煌满漠源滤滥滔溪溜滚滨粱滩慎誉塞谨福群殿辟障嫌嫁叠缝缠静碧璃墙撇嘉摧截誓境摘摔聚蔽慕暮蔑模榴榜榨歌遭酷酿酸磁愿需弊裳颗嗽蜻蜡蝇蜘赚锹锻舞稳算箩管僚鼻魄貌膜膊膀鲜疑馒裹敲豪膏遮腐瘦辣竭端旗精歉熄熔漆漂漫滴演漏慢寨赛察蜜谱嫩翠熊凳骡缩慧撕撒趣趟撑播撞撤增聪鞋蕉蔬横槽樱橡飘醋醉震霉瞒题暴瞎影踢踏踩踪蝶蝴嘱墨镇靠稻黎稿稼箱箭篇僵躺僻德艘膝膛熟摩颜毅糊遵潜潮懂额慰劈操燕薯薪薄颠橘整融醒餐嘴蹄器赠默镜赞篮邀衡膨雕磨凝辨辩糖糕燃澡激懒壁避缴戴擦鞠藏霜霞瞧蹈螺穗繁辫赢糟糠燥臂翼骤鞭覆蹦镰翻鹰警攀蹲颤瓣爆疆壤耀躁嚼嚷籍魔灌蠢霸露囊罐匕刁丐歹戈夭仑讥冗邓艾夯凸卢叭叽皿凹囚矢乍尔冯玄邦迂邢芋芍吏夷吁吕吆屹廷迄臼仲伦伊肋旭匈凫妆亥汛讳讶讹讼诀弛阱驮驯纫玖玛韧抠扼汞扳抡坎坞抑拟抒芙芜苇芥芯芭杖杉巫杈甫匣轩卤肖吱吠呕呐吟呛吻吭邑囤吮岖牡佑佃伺囱肛肘甸狈鸠彤灸刨庇吝庐闰兑灼沐沛汰沥沦汹沧沪忱诅诈罕屁坠妓姊妒纬玫卦坷坯拓坪坤拄拧拂拙拇拗茉昔苛苫苟苞茁苔枉枢枚枫杭郁矾奈奄殴歧卓昙哎咕呵咙呻咒咆咖帕账贬贮氛秉岳侠侥侣侈卑刽刹肴觅忿瓮肮肪狞庞疟疙疚卒氓炬沽沮泣泞泌沼怔怯宠宛衩祈诡帚屉弧弥陋陌函姆虱叁绅驹绊绎契贰玷玲珊拭拷拱挟垢垛拯荆茸茬荚茵茴荞荠荤荧荔栈柑栅柠枷勃柬砂泵砚鸥轴韭虐昧盹咧昵昭盅勋哆咪哟幽钙钝钠钦钧钮毡氢秕俏俄俐侯徊衍胚胧胎狰饵峦奕咨飒闺闽籽娄烁炫洼柒涎洛恃恍恬恤宦诫诬祠诲屏屎逊陨姚娜蚤骇耘耙秦匿埂捂捍袁捌挫挚捣捅埃耿聂荸莽莱莉莹莺梆栖桦栓桅桩贾酌砸砰砾殉逞哮唠哺剔蚌蚜畔蚣蚪蚓哩圃鸯唁哼唆峭唧峻赂赃钾铆氨秫笆俺赁倔殷耸舀豺豹颁胯胰脐脓逛卿鸵鸳馁凌凄衷郭斋疹紊瓷羔烙浦涡涣涤涧涕涩悍悯窍诺诽袒谆祟恕娩骏琐麸琉琅措捺捶赦埠捻掐掂掖掷掸掺勘聊娶菱菲萎菩萤乾萧萨菇彬梗梧梭曹酝酗厢硅硕奢盔匾颅彪眶晤曼晦冕啡畦趾啃蛆蚯蛉蛀唬啰唾啤啥啸崎逻崔崩婴赊铐铛铝铡铣铭矫秸秽笙笤偎傀躯兜衅徘徙舶舷舵敛翎脯逸凰猖祭烹庶庵痊阎阐眷焊焕鸿涯淑淌淮淆渊淫淳淤淀涮涵惦悴惋寂窒谍谐裆袱祷谒谓谚尉堕隅婉颇绰绷综绽缀巢琳琢琼揍堰揩揽揖彭揣搀搓壹搔葫募蒋蒂韩棱椰焚椎棺榔椭粟棘酣酥硝硫颊雳翘凿棠晰鼎喳遏晾畴跋跛蛔蜒蛤鹃喻啼喧嵌赋赎赐锉锌甥掰氮氯黍筏牍粤逾腌腋腕猩猬惫敦痘痢痪竣翔奠遂焙滞湘渤渺溃溅湃愕惶寓窖窘雇谤犀隘媒媚婿缅缆缔缕骚瑟鹉瑰搪聘斟靴靶蓖蒿蒲蓉楔椿楷榄楞楣酪碘硼碉辐辑频睹睦瞄嗜嗦暇畸跷跺蜈蜗蜕蛹嗅嗡嗤署蜀幌锚锥锨锭锰稚颓筷魁衙腻腮腺鹏肄猿颖煞雏馍馏禀痹廓痴靖誊漓溢溯溶滓溺寞窥窟寝褂裸谬媳嫉缚缤剿赘熬赫蔫摹蔓蔗蔼熙蔚兢榛榕酵碟碴碱碳辕辖雌墅嘁踊蝉嘀幔镀舔熏箍箕箫舆僧孵瘩瘟彰粹漱漩漾慷寡寥谭褐褪隧嫡缨撵撩撮撬擒墩撰鞍蕊蕴樊樟橄敷豌醇磕磅碾憋嘶嘲嘹蝠蝎蝌蝗蝙嘿幢镊镐稽篓膘鲤鲫褒瘪瘤瘫凛澎潭潦澳潘澈澜澄憔懊憎翩褥谴鹤憨履嬉豫缭撼擂擅蕾薛薇擎翰噩橱橙瓢蟥霍霎辙冀踱蹂蟆螃螟噪鹦黔穆篡篷篙篱儒膳鲸瘾瘸糙燎濒憾懈窿缰壕藐檬檐檩檀礁磷瞭瞬瞳瞪曙蹋蟋蟀嚎赡镣魏簇儡徽爵朦臊鳄糜癌懦豁臀藕藤瞻嚣鳍癞瀑襟璧戳攒孽蘑藻鳖蹭蹬簸簿蟹靡癣羹鬓攘蠕巍鳞糯譬霹躏髓蘸镶瓤矗";
    private static final String spoofChinese = "①乁②拾玎廠⑦卜亾叺仈勼凢ル孒劦釢叨叒彡纡迀扝仕笁汢財籿芐汏扙玙萭仩尒囗凧屾芉阣巛億個汋玖汎彶汐汍庅広匄閄義と迉弖己巳ふ衞竾囡飛刄習紁骉芗仹迋丼閞玞兲嘸沅抟囩紥兿朩伍伎廰芣忲吠岖呖尤伖苉車姖厊屯仳沍苆咓圵仯ㄖ狆罓赑禸渁見吘犇掱毝氣圱萇芢什爿圤囮仇币仍僅釿爪反夰父苁妗兇汾疺厷仺仴氏匢芡颩冄枃烏鳯芶妏陸汸焱潙乧忆忊計戶認杺呎吲忸妑芤隊刅姒狁予勧叒書抝砡刋沶沬沬击咑巧囸圤朳糼扔厾苷迣咕兯夲朮妸眪咗疠祐坧鈽瀧岼搣轧崬鉲苝颭鄴舊帥歸苴狚朩旪曱妽汀電呺甶甴史呮姎兇汈嘂叧叨嘆④泩妷秝坵苻扙笩佡們儀皛仔彵斥呱苸苁泠鼡甩茚泺呴茐冊氾迯處笗茑務笣饥炷巿竝閁蘭柈汥匯頭漢苧泬咜討冩讓礼訓怭议卂汜怺呞胒姄炪辽艿伮咖佋怶笾潑夃聖怼珆罞糾毋孧噝鉽鉶憅摃峙咭釦栲仛荖秇巩圾拡掃哋婸畼洱珙笀亞芷朽圤僟權過烥侢拹覀壓厭茬洧咟洊洏頁匠洿奪洃垯烮屍荿夾匦峫劃邁滭臸泚浈溮尘尖挘洸當皁汢圷蟲浀團哃铞阣洇扱嬤屿汎嵗冋豈碙荝禸蛧姩咮姺丟舙艸迁喬偉伝乒乓咻⑤茯沋浌娫件姙傷價妢澕卬汸钬沩洎洫姠姒後垳洀洤浍摋匼狣佱衆爺傘創肌朶卆佹洵旨萯茖洺哆踭脃匨沖栤圧庆洂嚠斉茭佽扆浐吷茺妄閉問闖咩並関洣燈詶汙汚茳肔饧杧興荢垨宅牸鮟講軍汻囵哝讽蔎汸浔哪卂浕导異孫俥陽荍阶隂汸奷洳妇恏咜媽戱羽觀歡荬葒汘级箹汜肔廵壽挵麥形琎悈昋逺違運荴抚墵技壞擾岠找纰扯歮趉莏坝貢糼哧菥抓汾熗涍汮拋投墳忼妔汸鬦戶涜梽沑赽殸妑蕔卻劫厊埖芹棼芲淓嚴廬崂尅蘇杆釭荰財籿莕极李昜浗浭娕荳倆婯悘宸励娝還姧唻涟荹堅猂饤珵溡呉莇縣里槑圎纩圍吖沌娖邮莮涃訬賗園厛玢欥嗚妑犼莂罓賬財針町哠莪亂悡禿莠俬烸娦诂軆哬泹訷莋伯伶砽彽沵炷莅柈裑唣仏菦沏役返悇唏唑唂鋖浛鄰岔肝肚肠亀凂誑沋角剼條卵島迊粄飮係訁岽匨畝況床厙療應唥適垿厗棄冶莣娴簡悶叛灶灿弚忹乷汽沃疺芶莈瀋冗懷沋赽唍浨宖窂究窮災悢姃晵评卟初涻識訴沴詞譯焄靈旣層杘屗呎挶妀張忌漈陸婀陳蒩胕仯岆妨怓涊勁鶏駆蒓纱妠罁訤枞妢衹鈫汸馿狃唪琓寰娬圊嫧哯錶規沬泷菝拣泹钽呷菗枴柂啪鍺嵿拆砽抵佝勢砲柆菈拦絆圉妱岥怶妭萚孡娸掫楛婼茂泙媌渶笵矗苆莖罞啉汥柸匱唽闆菘熗媾傑沭忱喪戓畵臥倳剌栆雨賣纩犸厠渀渏奮忲瓯泷悽轟頃啭斬囵軟菿悱埱肻歯些唬虏腎賢尙忹倶淉菋崐國誯畅眀昜昻敟凅筗咐苸嘄怺迡岸啱萜囉帜玪剀敗贩媾圖銱淛倁箠牧粅乖剮秆啝悸逶佳侍栱使唎蝂侄浈側憑喬姵貨畩哋廹質俽姃暀瓟彼徑葰舎唫掵釡妑采辤乳貪淰貧膚腓汥妕胀萠骰萉棴脅淍涽渔兎狐唿豞備飾怉饲變倞啍扂液庿椨疧剂郊廢凈吂倣尅唷閘閙鄭券捲啴炒炊忼烾爐沬淺琺绁菏跕汨怞泊沿垉炷瀉怺狔沸菠秡澤菭怖悻啪憐怪敩寶崈萣宜谉宙菅涳帘實鉽蓢詩肩房諴衬釤視話诞咰姟詳踺歗淥隶劇屆唰屈妶承掹箛陝夅限妺菇姐狌始駕傪艰線煉蒩細馶枳蔠驻袉袑經遦楱舂幇沴箥毐侀啩崶歭頙垮挎峸撓炡赴趙澢侹葀拴湁狣栺垫諍哜拚挖洝媈挪湈卙愅薦巷帶愺茧嗏巟汒蕩荣诂箶遖葯摽喖窉崬楿楂柏栁炷枾孄樹婹咸媙歪妍磚厘厚砌歃媔恧耍撁殘殃輕鴉湝揹戰點臨灠竪渻萷甞湜昐喳晎显啞萺眏暒葃嵔汃媦貴鎅渱虾蚁偲犸虽闆咽罵蕐洎姠铪吆嗑哪湠浹藅濺萜嗗鈔妕鋼钥溝啣矼湃看怇怎狌選适仯萫種偢萪偅復芉葮楩唡贷順俢湺娖侮倹俗俘信瑝葲媿埐搥浚盾待侓佷湏溆劍洮喰湓狚夝菢眫霡勉浹浉獨烄獄哏貿葾喼隢蚀饺饼塆將奨哀渟煷喥迹侹疮瘋疫疤恣儭堷渧湤聞阀阁槎養羙葁判鎹類洣湔渞屰縂煉怍垉灡珶洁葓灑浇浊狪恻冼萿哌洽媣哜樣詶渾哝珒恆恢恰悩悢舉覺媗厔営宪湥瑏苆愙蒄娪碥仸袓鉮柷誤诱説誦恳蹆旣偓昼曊跿葿陔篨険阮哇粩侇絪嬌伮泇哿盁湧怠渘垒垹絨結隢嬌浍给絡詻蕝烄統畊秏滟溙咮癍嫊蛬顽盏厞崂酨峬桭酨迀起鹽哨涅埋浞涃涓損嘟悊迣撿換梚慹恐壺挨恥耽塨嗹嗼嗬镬晉悪嫃框蓕澢秱株喬洮咯校劾樣艮鎍滒趚浢栗蓜趐媷脣嗄绌岥厡套豩烮姝顧轿珓頓斃臸枈棹慮盬緊黨曬眠哓鴨愰晌暈螡哨哭慁喚娿呃罢峯園賊贿錢钳鑽鉄玪鉛蒛氧特犠慥塖敵秤蒩積秧秩稱秘透毣笑笋債徣惪掎傾箌倘倶倡糇椨俻惓揵溴射匑息徙俆舰舱瘢航蒤嗱嗲嬡頌暡脆脂洶胳賍烄悩狸哴漨畱皺皒戀桨槳缞滈席痽蓙脊症疒疾庝疲效蓠瑭粢涼跕剖競蔀臱膂畜閱饈甁拳帉料谥凲栲烘煩燒烛煙递濤淅崂氿渉消滘嗨凃浴捊蓅潤烺锓涨烫悀圄佾珻哾嗐寬傢宵匽濱搾嫆宰案埥蓢渚渎傓襪袖垉被祥錁誰蜩寃涼談谊剝垦蹍涺屑弜夌匋陥婄娯娘嗵能難預鎟涓绣験继浗理唪陼媌域殗啑棑鋽碓蓷锨涭嘋匋稤掊帹啌探琚崛轵樭著嘞曂萠囉箘婇匋匊泙菠營悈夢哨烸撿梳珶硧慦諨嘌嘁摤聾袭墭膤辅唡歔雀漟瑺匙曟諍侎眼悬嘢菈脕啄岠跞畧虵蔂晿漶惟崖崭漴圜恫铲檭甛悡犁簃苯茏廸苻苐勄莋袋滺偿耦偸您售渟媥徦嘚銜盤船斜盉鸽悉慾埰領腳脖臉脫潒夠猜蕏獵貓掹陥菅凑諴毫嫲癢痕蓢嫝滽蔍盜嶂獍啇蔟嫙朢卛着葢秥粗粒斷彅獣凊婖啉殗渠渐婫渔匋液惔堔嘙樑椮凊厝慙悼惧惕驚參遦簆寄蹜窰滵湈巟禍洣曃噉廜弹隨疍湰陻殙嬸頸绩緒續騏繩惟婂皗淥噖癍櫕窾堪溚嗒樾趁趋趫諟諟博揭禧揷啾溲煑瑗裁擱嵝搅楃渘凘剘剘聅潵惹髒噶蓳匍擏茐落謿辜葵蜯諆淔潹掎椒錁輥婂堋琮惠惑腷廚厦哽確雁殖煭雄暫蕥輩蕜橴媈敞賞礃啨濐朂糧濆瞐喇喁諴憬践瓞垉遗蛙咮蜓曷嵔遄糇諨萺帾婄嫼铸舗嗹销鎻鋤煱琇峯銳短潪毯鹅乗哨珵浠挩筺等茿憡篩茼荅荕筝謸傅簰堡潗潐徬储奧街懲御揗艇忬畨释噙臘裨腔噜磆糇嘫镵裝蠻僦痌僮闊僐羨普粪澊檤嶒熖港煳碴濕溫渇磆塆喥遊稵漑濆巟媠隗揄慨剨寒冨窜窉囱猵裕褲峮塮愮嗛屬屢強粥疏隔隙絮溲憕葮緩揙騙緣瑞魂肆摂嗼瑱搏禢鼔擺携搬愮鎬溏摊祘懄鹊藍募募莑蓄懞篜獻噤椘想槐榆溇漑攋絒憾碍碑誶湴涴碌檑蕶霚雹瀭督齡鉴聙腄棌啚愚煖擝歇暗燳跨朓蛫蕗茛遣睋蜂鎟置嶵罩措唶囉腄婂楗涺婑辭婤僽薵簽彅毇舅癙慛傻潒躱嶶匬滛崾睲腹駦蹆触解醬痰廉噺韻嬑悢薮煎愬濨湈瑝慲嗼羱慮嚂瑫渓媹蔉璸粱滩慎謍噻殣湢羣殿澼障溓糘疉漨瀍靜碧璃嫱潎嘉凗截誓璄擿摔藂幣慕暮篾嗼媹徬搾戨蹧酷酿酸濨蒝濡弊裳錁嗽蜻臘蝇倁賺锹葮橆穏匴儸涫獠嬶魄邈嗼膊嫎鮮寲獌裹毃濠膏嗻腐痩辣竭鍴旗棈嗛熄嫆漆慓嫚嘀湮屚嫚寨噻镲滵鐠嫰濢熋凳骡縮慧凘潵趣趟撐譒獞徹熷聪嚡蕉蔬橫槽璎潒彯齰酔震苺慲趧懪磍影踢沓棌琮渫箶瞩嚜鎮靠稲黎鎬糘葙箭萹壃躺僻徳艘膝膛孰嚤顔藙煳噂濳謿慬额墛噼懆嬿薯蕲薄颠橘整瀜瑆爘觜渧噐熷默傹瓒藍撽蘅膨鵰嚤凝辧辮溏溔嘫璪噭攋壁鐴儌瀻攃鞠蔵灀葭趭稲螺穗瀿辮赢蹧嵻璪臂翼骤鞭覆嘣镰飜鹰檠襻壿顫瓣嚗彊壤耀璪嚼孃籍嚤潅蠢覇蕗灢潅匕刁丐歹戈夭仑讥冗郰銰夯凸盧朳叽皿凹囚矢咋尒溤玆綁扜郉芋芍吏夷吁焒吆屹侹迄臼仲囵吚叻旮匈凫妝亥卂讳冴讹讼吷肔阱駞紃纫玖犸韧摳扼汞扳囵坎坞抑抳杼芙蕪苇芥芯妑粀杉莁杈甫匣蓒卤肖汥吠嘔妠荶濸沕妔邑囤吮岖牡祐佃伺囱釭肘甸狈鸠浵灸铇庇悋廬潤兌灼沐沛呔沥囵汹獊戶忱蒩怍癷庇墜妓姊妒纬坆卦坷坯沰岼堒拄拧拂炪拇拗茉厝岢苫苟苞茁苔忹枢枚猦忼喐矾柰奄瓯忮婥昙餀咕哬茏呻咒垉咖啪账貶贮氛秉捳浹侥佀侈萆刽閷肴觅忿瓮肮肪狞厐疟疙咎卒氓岠钴沮淇泞泌沼姃愜寵宛衩祈詭帚屟弧弥陋帞凾姆虱叁訷驹绊绎契贰玷玪姍拭洘珙挾垢垛拯荊茸茬荚筃茴荞荠荤荧荔棧柑栅柠枷勃柬唦泵砚瓯轴韭疟昧盹咧昵昭盅勛哆咪喲豳鈣沌妠钦呁妞毡氢秕佾皒俐糇佪衍胚胧胎狰聶欒奕恣飒閨閩籽溇爍炫哇柒涎詻恃恍恬恤宦诫莁祠诲屛屍遜殒烑哪蚤骇秐耙蓁嫟埂圄捍媴捌挫挚搗硧埃耿嗫荸漭莱莉瑩莺梆栖桦拴桅桩贾酌咂泙砾咰浧涍唠誧剔蚌蚜溿蚣蚪蚓哩圃鴦唁涥逡峭唧浚赂賍钾铆氨秫笆唵賃崛殷聳舀豺豹頒胯胰脐哝迋卿袉鴛浽夌凄衷漷斋疹紊瓷餻絡浦煱涣涤涧珶澀悍悯竅喏诽袒谆祟恕娩浚鎖麸琉哴措捺腄赦埠捻拤掂掖掷掸傪勘窷婜夌婔崣箁萤墘簘蕯菇彬梗梧逡蓸酝酗厢硅碩奢盔匾颅滮洭晤嫚珻冕啡畦趾肻蛆蚯蛉蛀唬啰唾啤倽啸崎羅慛镚璎赊铐铛焒铡铣佲矫秸秽笙笤偎傀軀兠衅棑徙舶舷舵潋翎脯逸瑝猖祭烹庶庵痊閻闡眷猂焕鴻涯蔋淌准淆棩婬錞菸淀涮凾惦悴惋寂窒媟喈裆袱祷谒媦谚墛憜隅啘櫇焯绷琮綻綴漅啉琢琼楱堰揩灠揖憉遄搀髊壹搔煳募蔣渧韓棱倻焚椎菅蓈椭粟棘酣酥硝硫颊雳趬凿橖晰鼎喳遏涼帱柭跛蛔蜒蛤鵑喻渧媗嵌賦赎賜锉锌甥掰氮氯黍筏牍粵揄腌腋腕睲猬惫敦哣痢痪浚翔奠嬘焙滞湘渤緲潰溅湃愕瑝寓窖僒雇谤犀隘媒媚胥缅灠缔缕騒瑟鹉瑰搪娉斟靴靶蓖蒿蒲嫆楔椿揩榄楞楣絡碘硼淍辐辑頻睹睦媌嗜嗦叚畸跷跺蜈窩蜕蛹溴滃嗤署蜀縨锚锥锨锭锰雉颓筷魁衙膩腮腺鵬肄猿颕繺雛馍馏禀痹霩痴靖誊漓溢溯嫆滓溺寞窺崫寑啩裸繆媳嫉缚缤剿赘熬赫蔫摹嫚蔗蔼凞墛兢榛榕酵渫楂碱湠辕轄雌墅嘁踊蝉嘀幔镀婖熏箍箕箫舆僧孵瘩瘟彰濢漱漩羕嵻寡寥谭褐蹆隧嫡缨撵嫽撮毳檎墩撰鞍惢藴樊樟橄敷豌錞溘嫎碾憋凘謿嘹蝠蝎蝌蝗蝙潶幢嗫鎬稽溇鏢鲤鲫褒癟媹瘫凛澎憛潦奧瀋瞮瀾僜憔襖璔翩褥谴鹤憨履嬉豫缭撼檑擅檑薛薇擎翰噩廚橙瓢蟥靃霎辙冀踱蹂蟆螃螟璪鹦黔穆篡篷禞篱濡膳鯨瘾瘸鐰獠濒憾澥窿缰壕邈檬檐檩檀礁潾瞭橓瞳簦曙蹋蟋蟀嚎赡镣蘶簇儡幑嚼朦臊鳄糜癌穤豁臀耦駦瞻嚣鳍癞鑤噤璧戥瓒糵嚤藻鳖竲簦簸簿蟹靡癣羹鬓攘蠕蘶潾穤譬噼躏髓蘸镶瓤矗";
    private EntityPlayer target;
    private String lastChat = "";
    private int ticks;

    @Override
    protected void onEnable() {
        lastChat = "";
    }

    @EventLink()
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        if (faDian.getValue())
            if (mc.thePlayer.ticksExisted >= 100 && mc.thePlayer.ticksExisted % 40 == 0)
                mc.thePlayer.sendChatMessageNoEvent((publicChat.getValue() ? "@" : "") + "[" + Client.NAME + "]" + lyrics[RandomUtils.nextInt(0, lyrics.length)] + "<" + RandomUtil.randomName() + ">");

        if (mode.getValue().getName().equalsIgnoreCase("urticaria")) {
            if (target != null) {
                if (mc.thePlayer.getHealth() <= 0 || mc.thePlayer.isDead || mc.thePlayer.getDistanceToEntity(target) > KillAura.INSTANCE.range.getValue().floatValue()) {
                    target = null;
                } else if (target.isDead || target.getHealth() <= 0) {
                    mc.thePlayer.sendChatMessage(String.format((publicChat.getValue() ? "@" : "") + "[" + Client.NAME + "] | 不收徒 B站灌注 _moran_ " + target.getName() + " Your configuration is too Ｌ 获取同款->shop.xmz.lol <" + RandomUtil.randomName() + ">", PlayerUtil.name(target)));
                    target = null;
                }
            }
        } else if (target != null && !mc.theWorld.playerEntities.contains(target)) {

            if (ticks >= delay.getValue().intValue() + Math.random() * 5 && !BadPacketsComponent.bad()) {
                String insult = "";

                switch (mode.getValue().getName()) {
                    case "Default":
                        insult = (publicChat.getValue() ? "@" : "") + target.getName() + " " + defaultInsults[RandomUtils.nextInt(0, defaultInsults.length)];
                        break;

                    case "Watchdog":
                        insult = "[STAFF] [WATCHDOG] %s reeled in.";
                        break;

                    case "WhatsApp":
                        insult = whatsAppInsults[RandomUtils.nextInt(0, whatsAppInsults.length)];
                        break;
                }

                mc.thePlayer.sendChatMessage(String.format(insult, PlayerUtil.name(target)));
                target = null;
            }

            ticks++;
        }
    };

    @EventLink()
    public final Listener<AttackEvent> onAttack = event -> {

        final Entity target = event.getTarget();

        if (target instanceof EntityPlayer) {
            this.target = (EntityPlayer) target;
            ticks = 0;
        }
    };

    @EventLink()
    public final Listener<WorldChangeEvent> onWorldChange = event -> {
        target = null;
        ticks = 0;
    };

    @EventLink
    private final Listener<PacketReceiveEvent> onPacketReceive = event -> {
        if (!chatBypass.getValue() || !bypassMode.getValue().getName().equalsIgnoreCase("Double Check") || lastChat.isEmpty()) return;

        final Packet<?> packet = event.getPacket();

        if (packet instanceof S02PacketChat) {
            final String chat = ((S02PacketChat) packet).getChatComponent().getFormattedText();

            if (chat.contains("发言包含不合法内容") && !chat.contains(":") && !chat.contains(">")) {
                mc.thePlayer.sendChatMessage(lastChat);
                event.setCancelled();
            }
        }
    };

    @EventLink
    private final Listener<ChatInputEvent> onChatInput = event -> {
        if (chatBypass.getValue()) {
            final String input = event.getMessage();
            if (input.startsWith(".") || input.startsWith("`") || input.startsWith("/")) return;

            if (bypassMode.getValue().getName().equalsIgnoreCase("Illegal Chat")) {
                StringBuilder sb = new StringBuilder();

                for (char c : input.toCharArray()) {
                    if (c >= 33 && c <= 128 && (!publicChat.getValue() || c != '@'))
                        sb.append(Character.toChars(c + 65248));
                    else {
                        final int index = normalChinese.indexOf(c);

                        if (index >= 0) {
                            sb.append(spoofChinese.charAt(index));
                        } else {
                            sb.append(c);
                        }
                    }
                }

                mc.thePlayer.sendChatMessageNoEvent(sb.toString());

                event.setCancelled();
            } else {
                lastChat = event.getMessage();
            }
        }
    };
}