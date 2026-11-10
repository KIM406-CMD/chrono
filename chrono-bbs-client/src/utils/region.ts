export interface RegionNode {
  value: string
  label: string
  children?: RegionNode[]
}

export const regionOptions: RegionNode[] = [
  {
    value: '北京市',
    label: '北京市',
    children: [
      {
        value: '北京市',
        label: '北京市',
        children: [
          { value: '东城区', label: '东城区' },
          { value: '西城区', label: '西城区' },
          { value: '朝阳区', label: '朝阳区' },
          { value: '丰台区', label: '丰台区' },
          { value: '石景山区', label: '石景山区' },
          { value: '海淀区', label: '海淀区' },
          { value: '通州区', label: '通州区' },
          { value: '昌平区', label: '昌平区' },
        ],
      },
    ],
  },
  {
    value: '天津市',
    label: '天津市',
    children: [
      {
        value: '天津市',
        label: '天津市',
        children: [
          { value: '和平区', label: '和平区' },
          { value: '河东区', label: '河东区' },
          { value: '河西区', label: '河西区' },
          { value: '南开区', label: '南开区' },
          { value: '河北区', label: '河北区' },
          { value: '滨海新区', label: '滨海新区' },
        ],
      },
    ],
  },
  {
    value: '河北省',
    label: '河北省',
    children: [
      {
        value: '石家庄市',
        label: '石家庄市',
        children: [
          { value: '长安区', label: '长安区' },
          { value: '桥西区', label: '桥西区' },
          { value: '新华区', label: '新华区' },
          { value: '井陉矿区', label: '井陉矿区' },
          { value: '裕华区', label: '裕华区' },
        ],
      },
      {
        value: '唐山市',
        label: '唐山市',
        children: [
          { value: '路南区', label: '路南区' },
          { value: '路北区', label: '路北区' },
          { value: '古冶区', label: '古冶区' },
        ],
      },
      {
        value: '廊坊市',
        label: '廊坊市',
        children: [
          { value: '安次区', label: '安次区' },
          { value: '广阳区', label: '广阳区' },
          { value: '三河市', label: '三河市' },
        ],
      },
      {
        value: '保定市',
        label: '保定市',
        children: [
          { value: '竞秀区', label: '竞秀区' },
          { value: '莲池区', label: '莲池区' },
        ],
      },
    ],
  },
  {
    value: '山西省',
    label: '山西省',
    children: [
      {
        value: '太原市',
        label: '太原市',
        children: [
          { value: '小店区', label: '小店区' },
          { value: '迎泽区', label: '迎泽区' },
          { value: '杏花岭区', label: '杏花岭区' },
        ],
      },
      {
        value: '大同市',
        label: '大同市',
        children: [
          { value: '平城区', label: '平城区' },
          { value: '云冈区', label: '云冈区' },
        ],
      },
    ],
  },
  {
    value: '辽宁省',
    label: '辽宁省',
    children: [
      {
        value: '沈阳市',
        label: '沈阳市',
        children: [
          { value: '和平区', label: '和平区' },
          { value: '沈河区', label: '沈河区' },
          { value: '大东区', label: '大东区' },
          { value: '皇姑区', label: '皇姑区' },
          { value: '铁西区', label: '铁西区' },
        ],
      },
      {
        value: '大连市',
        label: '大连市',
        children: [
          { value: '中山区', label: '中山区' },
          { value: '西岗区', label: '西岗区' },
          { value: '沙河口区', label: '沙河口区' },
        ],
      },
    ],
  },
  {
    value: '上海市',
    label: '上海市',
    children: [
      {
        value: '上海市',
        label: '上海市',
        children: [
          { value: '黄浦区', label: '黄浦区' },
          { value: '徐汇区', label: '徐汇区' },
          { value: '长宁区', label: '长宁区' },
          { value: '静安区', label: '静安区' },
          { value: '普陀区', label: '普陀区' },
          { value: '虹口区', label: '虹口区' },
          { value: '杨浦区', label: '杨浦区' },
          { value: '浦东新区', label: '浦东新区' },
          { value: '闵行区', label: '闵行区' },
          { value: '宝山区', label: '宝山区' },
        ],
      },
    ],
  },
  {
    value: '江苏省',
    label: '江苏省',
    children: [
      {
        value: '南京市',
        label: '南京市',
        children: [
          { value: '玄武区', label: '玄武区' },
          { value: '秦淮区', label: '秦淮区' },
          { value: '建邺区', label: '建邺区' },
          { value: '鼓楼区', label: '鼓楼区' },
          { value: '栖霞区', label: '栖霞区' },
          { value: '雨花台区', label: '雨花台区' },
        ],
      },
      {
        value: '苏州市',
        label: '苏州市',
        children: [
          { value: '姑苏区', label: '姑苏区' },
          { value: '虎丘区', label: '虎丘区' },
          { value: '吴中区', label: '吴中区' },
          { value: '相城区', label: '相城区' },
          { value: '工业园区', label: '工业园区' },
        ],
      },
      {
        value: '无锡市',
        label: '无锡市',
        children: [
          { value: '锡山区', label: '锡山区' },
          { value: '惠山区', label: '惠山区' },
          { value: '滨湖区', label: '滨湖区' },
        ],
      },
    ],
  },
  {
    value: '浙江省',
    label: '浙江省',
    children: [
      {
        value: '杭州市',
        label: '杭州市',
        children: [
          { value: '上城区', label: '上城区' },
          { value: '拱墅区', label: '拱墅区' },
          { value: '西湖区', label: '西湖区' },
          { value: '滨江区', label: '滨江区' },
          { value: '余杭区', label: '余杭区' },
        ],
      },
      {
        value: '宁波市',
        label: '宁波市',
        children: [
          { value: '海曙区', label: '海曙区' },
          { value: '江北区', label: '江北区' },
          { value: '北仑区', label: '北仑区' },
        ],
      },
    ],
  },
  {
    value: '安徽省',
    label: '安徽省',
    children: [
      {
        value: '合肥市',
        label: '合肥市',
        children: [
          { value: '瑶海区', label: '瑶海区' },
          { value: '庐阳区', label: '庐阳区' },
          { value: '蜀山区', label: '蜀山区' },
          { value: '包河区', label: '包河区' },
        ],
      },
    ],
  },
  {
    value: '福建省',
    label: '福建省',
    children: [
      {
        value: '福州市',
        label: '福州市',
        children: [
          { value: '鼓楼区', label: '鼓楼区' },
          { value: '台江区', label: '台江区' },
          { value: '仓山区', label: '仓山区' },
        ],
      },
      {
        value: '厦门市',
        label: '厦门市',
        children: [
          { value: '思明区', label: '思明区' },
          { value: '海沧区', label: '海沧区' },
          { value: '湖里区', label: '湖里区' },
        ],
      },
    ],
  },
  {
    value: '山东省',
    label: '山东省',
    children: [
      {
        value: '济南市',
        label: '济南市',
        children: [
          { value: '历下区', label: '历下区' },
          { value: '市中区', label: '市中区' },
          { value: '槐荫区', label: '槐荫区' },
        ],
      },
      {
        value: '青岛市',
        label: '青岛市',
        children: [
          { value: '市南区', label: '市南区' },
          { value: '市北区', label: '市北区' },
          { value: '黄岛区', label: '黄岛区' },
        ],
      },
    ],
  },
  {
    value: '河南省',
    label: '河南省',
    children: [
      {
        value: '郑州市',
        label: '郑州市',
        children: [
          { value: '中原区', label: '中原区' },
          { value: '二七区', label: '二七区' },
          { value: '金水区', label: '金水区' },
          { value: '惠济区', label: '惠济区' },
        ],
      },
    ],
  },
  {
    value: '湖北省',
    label: '湖北省',
    children: [
      {
        value: '武汉市',
        label: '武汉市',
        children: [
          { value: '江岸区', label: '江岸区' },
          { value: '江汉区', label: '江汉区' },
          { value: '硚口区', label: '硚口区' },
          { value: '汉阳区', label: '汉阳区' },
          { value: '武昌区', label: '武昌区' },
          { value: '洪山区', label: '洪山区' },
        ],
      },
    ],
  },
  {
    value: '湖南省',
    label: '湖南省',
    children: [
      {
        value: '长沙市',
        label: '长沙市',
        children: [
          { value: '芙蓉区', label: '芙蓉区' },
          { value: '天心区', label: '天心区' },
          { value: '岳麓区', label: '岳麓区' },
          { value: '开福区', label: '开福区' },
          { value: '雨花区', label: '雨花区' },
        ],
      },
    ],
  },
  {
    value: '广东省',
    label: '广东省',
    children: [
      {
        value: '广州市',
        label: '广州市',
        children: [
          { value: '荔湾区', label: '荔湾区' },
          { value: '越秀区', label: '越秀区' },
          { value: '海珠区', label: '海珠区' },
          { value: '天河区', label: '天河区' },
          { value: '白云区', label: '白云区' },
          { value: '黄埔区', label: '黄埔区' },
        ],
      },
      {
        value: '深圳市',
        label: '深圳市',
        children: [
          { value: '罗湖区', label: '罗湖区' },
          { value: '福田区', label: '福田区' },
          { value: '南山区', label: '南山区' },
          { value: '宝安区', label: '宝安区' },
          { value: '龙岗区', label: '龙岗区' },
          { value: '龙华区', label: '龙华区' },
        ],
      },
      {
        value: '东莞市',
        label: '东莞市',
        children: [
          { value: '莞城街道', label: '莞城街道' },
          { value: '南城街道', label: '南城街道' },
        ],
      },
    ],
  },
  {
    value: '广西壮族自治区',
    label: '广西壮族自治区',
    children: [
      {
        value: '南宁市',
        label: '南宁市',
        children: [
          { value: '兴宁区', label: '兴宁区' },
          { value: '青秀区', label: '青秀区' },
        ],
      },
    ],
  },
  {
    value: '海南省',
    label: '海南省',
    children: [
      {
        value: '海口市',
        label: '海口市',
        children: [
          { value: '秀英区', label: '秀英区' },
          { value: '龙华区', label: '龙华区' },
        ],
      },
      {
        value: '三亚市',
        label: '三亚市',
        children: [
          { value: '海棠区', label: '海棠区' },
          { value: '吉阳区', label: '吉阳区' },
        ],
      },
    ],
  },
  {
    value: '重庆市',
    label: '重庆市',
    children: [
      {
        value: '重庆市',
        label: '重庆市',
        children: [
          { value: '渝中区', label: '渝中区' },
          { value: '江北区', label: '江北区' },
          { value: '沙坪坝区', label: '沙坪坝区' },
          { value: '九龙坡区', label: '九龙坡区' },
          { value: '南岸区', label: '南岸区' },
          { value: '渝北区', label: '渝北区' },
        ],
      },
    ],
  },
  {
    value: '四川省',
    label: '四川省',
    children: [
      {
        value: '成都市',
        label: '成都市',
        children: [
          { value: '锦江区', label: '锦江区' },
          { value: '青羊区', label: '青羊区' },
          { value: '金牛区', label: '金牛区' },
          { value: '武侯区', label: '武侯区' },
          { value: '成华区', label: '成华区' },
          { value: '龙泉驿区', label: '龙泉驿区' },
        ],
      },
    ],
  },
  {
    value: '贵州省',
    label: '贵州省',
    children: [
      {
        value: '贵阳市',
        label: '贵阳市',
        children: [
          { value: '南明区', label: '南明区' },
          { value: '云岩区', label: '云岩区' },
        ],
      },
    ],
  },
  {
    value: '云南省',
    label: '云南省',
    children: [
      {
        value: '昆明市',
        label: '昆明市',
        children: [
          { value: '五华区', label: '五华区' },
          { value: '盘龙区', label: '盘龙区' },
          { value: '官渡区', label: '官渡区' },
        ],
      },
    ],
  },
  {
    value: '陕西省',
    label: '陕西省',
    children: [
      {
        value: '西安市',
        label: '西安市',
        children: [
          { value: '新城区', label: '新城区' },
          { value: '碑林区', label: '碑林区' },
          { value: '莲湖区', label: '莲湖区' },
          { value: '雁塔区', label: '雁塔区' },
          { value: '未央区', label: '未央区' },
        ],
      },
    ],
  },
  {
    value: '甘肃省',
    label: '甘肃省',
    children: [
      {
        value: '兰州市',
        label: '兰州市',
        children: [
          { value: '城关区', label: '城关区' },
          { value: '七里河区', label: '七里河区' },
        ],
      },
    ],
  },
]