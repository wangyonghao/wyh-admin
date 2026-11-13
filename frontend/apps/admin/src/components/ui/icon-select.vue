<script setup lang="ts">
import type { Component } from 'vue';

import { computed, markRaw, ref } from 'vue';

import {
  AccessibilityOutline,
  AddCircleOutline,
  AddOutline,
  AirplaneOutline,
  AlarmOutline,
  AlertCircleOutline,
  AnalyticsOutline,
  AppsOutline,
  BagOutline,
  BarChartOutline,
  BarcodeOutline,
  BicycleOutline,
  BluetoothOutline,
  BoatOutline,
  BookmarkOutline,
  BookOutline,
  BrushOutline,
  BugOutline,
  BuildOutline,
  BulbOutline,
  BusinessOutline,
  CafeOutline,
  CalendarOutline,
  CameraOutline,
  CardOutline,
  CarOutline,
  CartOutline,
  CashOutline,
  ChatbubbleOutline,
  ChatbubblesOutline,
  CheckmarkCircleOutline,
  ClipboardOutline,
  CloseCircleOutline,
  CloseOutline,
  CloudDownloadOutline,
  CloudOutline,
  CloudUploadOutline,
  CodeOutline,
  CodeSlashOutline,
  CogOutline,
  ColorPaletteOutline,
  CompassOutline,
  ConstructOutline,
  ContractOutline,
  CopyOutline,
  CreateOutline,
  CubeOutline,
  CutOutline,
  DesktopOutline,
  DocumentOutline,
  DocumentTextOutline,
  DownloadOutline,
  EarthOutline,
  EllipseOutline,
  EllipsisHorizontalOutline,
  EllipsisVerticalOutline,
  EnterOutline,
  ExitOutline,
  ExpandOutline,
  ExtensionPuzzleOutline,
  EyeOffOutline,
  EyeOutline,
  FingerPrintOutline,
  FitnessOutline,
  FlagOutline,
  FlashOutline,
  FlaskOutline,
  FlowerOutline,
  FolderOpenOutline,
  FolderOutline,
  GitBranchOutline,
  GitCommitOutline,
  GitMergeOutline,
  GitPullRequestOutline,
  GlobeOutline,
  GridOutline,
  HammerOutline,
  HappyOutline,
  HardwareChipOutline,
  HeartOutline,
  HelpCircleOutline,
  HelpOutline,
  HomeOutline,
  ImageOutline,
  InformationCircleOutline,
  KeyOutline,
  LaptopOutline,
  LayersOutline,
  LeafOutline,
  LibraryOutline,
  LinkOutline,
  ListOutline,
  LocationOutline,
  LockClosedOutline,
  LockOpenOutline,
  LogInOutline,
  LogOutOutline,
  MailOutline,
  MapOutline,
  MedkitOutline,
  MenuOutline,
  MoonOutline,
  MusicalNotesOutline,
  NavigateOutline,
  NewspaperOutline,
  NotificationsOutline,
  OptionsOutline,
  PawOutline,
  PeopleOutline,
  PersonOutline,
  PhonePortraitOutline,
  PieChartOutline,
  PlanetOutline,
  PricetagOutline,
  PrintOutline,
  PulseOutline,
  QrCodeOutline,
  RainyOutline,
  RefreshOutline,
  ReloadOutline,
  RemoveCircleOutline,
  ReorderFourOutline,
  ReorderThreeOutline,
  ReorderTwoOutline,
  RestaurantOutline,
  RocketOutline,
  SadOutline,
  SaveOutline,
  ScanOutline,
  SchoolOutline,
  SearchOutline,
  SendOutline,
  ServerOutline,
  SettingsOutline,
  ShapesOutline,
  ShareOutline,
  ShieldOutline,
  SnowOutline,
  SpeedometerOutline,
  SquareOutline,
  StarOutline,
  StatsChartOutline,
  StorefrontOutline,
  SunnyOutline,
  SyncOutline,
  TabletPortraitOutline,
  TerminalOutline,
  ThumbsDownOutline,
  ThumbsUpOutline,
  ThunderstormOutline,
  TimeOutline,
  ToggleOutline,
  TrashOutline,
  TrendingDownOutline,
  TrendingUpOutline,
  TriangleOutline,
  TrophyOutline,
  TvOutline,
  VideocamOutline,
  WalkOutline,
  WalletOutline,
  WarningOutline,
  WatchOutline,
  WifiOutline,
} from '@vicons/ionicons5';

import { Input } from '#/ui/input';
import { Popover, PopoverContent, PopoverTrigger } from '#/ui/popover';

defineOptions({ name: 'IconSelect' });
const modelValue = defineModel<string>({ default: '' });
const searchText = ref('');

// 图标列表
const iconList: { component: Component; label: string; name: string }[] = [
  { name: 'HomeOutline', label: '首页', component: markRaw(HomeOutline) },
  {
    name: 'SettingsOutline',
    label: '设置',
    component: markRaw(SettingsOutline),
  },
  { name: 'PersonOutline', label: '用户', component: markRaw(PersonOutline) },
  { name: 'PeopleOutline', label: '用户组', component: markRaw(PeopleOutline) },
  { name: 'KeyOutline', label: '密钥', component: markRaw(KeyOutline) },
  { name: 'ShieldOutline', label: '安全', component: markRaw(ShieldOutline) },
  {
    name: 'DocumentOutline',
    label: '文档',
    component: markRaw(DocumentOutline),
  },
  { name: 'FolderOutline', label: '文件夹', component: markRaw(FolderOutline) },
  {
    name: 'FolderOpenOutline',
    label: '打开文件夹',
    component: markRaw(FolderOpenOutline),
  },
  { name: 'GridOutline', label: '网格', component: markRaw(GridOutline) },
  { name: 'AppsOutline', label: '应用', component: markRaw(AppsOutline) },
  { name: 'ListOutline', label: '列表', component: markRaw(ListOutline) },
  { name: 'MenuOutline', label: '菜单', component: markRaw(MenuOutline) },
  { name: 'SearchOutline', label: '搜索', component: markRaw(SearchOutline) },
  { name: 'AddOutline', label: '添加', component: markRaw(AddOutline) },
  { name: 'CreateOutline', label: '创建', component: markRaw(CreateOutline) },
  { name: 'TrashOutline', label: '删除', component: markRaw(TrashOutline) },
  {
    name: 'CloudUploadOutline',
    label: '上传',
    component: markRaw(CloudUploadOutline),
  },
  {
    name: 'CloudDownloadOutline',
    label: '下载',
    component: markRaw(CloudDownloadOutline),
  },
  { name: 'ImageOutline', label: '图片', component: markRaw(ImageOutline) },
  { name: 'CameraOutline', label: '相机', component: markRaw(CameraOutline) },
  {
    name: 'VideocamOutline',
    label: '视频',
    component: markRaw(VideocamOutline),
  },
  {
    name: 'MusicalNotesOutline',
    label: '音乐',
    component: markRaw(MusicalNotesOutline),
  },
  {
    name: 'DocumentTextOutline',
    label: '文档文本',
    component: markRaw(DocumentTextOutline),
  },
  {
    name: 'NewspaperOutline',
    label: '新闻',
    component: markRaw(NewspaperOutline),
  },
  { name: 'BookOutline', label: '书籍', component: markRaw(BookOutline) },
  {
    name: 'BookmarkOutline',
    label: '书签',
    component: markRaw(BookmarkOutline),
  },
  {
    name: 'CalendarOutline',
    label: '日历',
    component: markRaw(CalendarOutline),
  },
  { name: 'TimeOutline', label: '时间', component: markRaw(TimeOutline) },
  { name: 'AlarmOutline', label: '闹钟', component: markRaw(AlarmOutline) },
  {
    name: 'NotificationsOutline',
    label: '通知',
    component: markRaw(NotificationsOutline),
  },
  {
    name: 'ChatbubbleOutline',
    label: '聊天',
    component: markRaw(ChatbubbleOutline),
  },
  {
    name: 'ChatbubblesOutline',
    label: '聊天组',
    component: markRaw(ChatbubblesOutline),
  },
  { name: 'MailOutline', label: '邮件', component: markRaw(MailOutline) },
  { name: 'SendOutline', label: '发送', component: markRaw(SendOutline) },
  { name: 'ShareOutline', label: '分享', component: markRaw(ShareOutline) },
  { name: 'LinkOutline', label: '链接', component: markRaw(LinkOutline) },
  { name: 'GlobeOutline', label: '全球', component: markRaw(GlobeOutline) },
  { name: 'MapOutline', label: '地图', component: markRaw(MapOutline) },
  {
    name: 'LocationOutline',
    label: '位置',
    component: markRaw(LocationOutline),
  },
  {
    name: 'CompassOutline',
    label: '指南针',
    component: markRaw(CompassOutline),
  },
  {
    name: 'NavigateOutline',
    label: '导航',
    component: markRaw(NavigateOutline),
  },
  { name: 'CarOutline', label: '汽车', component: markRaw(CarOutline) },
  {
    name: 'AirplaneOutline',
    label: '飞机',
    component: markRaw(AirplaneOutline),
  },
  { name: 'BoatOutline', label: '船', component: markRaw(BoatOutline) },
  {
    name: 'BicycleOutline',
    label: '自行车',
    component: markRaw(BicycleOutline),
  },
  { name: 'WalkOutline', label: '步行', component: markRaw(WalkOutline) },
  { name: 'FitnessOutline', label: '健身', component: markRaw(FitnessOutline) },
  { name: 'HeartOutline', label: '心形', component: markRaw(HeartOutline) },
  { name: 'StarOutline', label: '星星', component: markRaw(StarOutline) },
  { name: 'TrophyOutline', label: '奖杯', component: markRaw(TrophyOutline) },
  { name: 'FlagOutline', label: '旗帜', component: markRaw(FlagOutline) },
  {
    name: 'PricetagOutline',
    label: '标签',
    component: markRaw(PricetagOutline),
  },
  { name: 'CartOutline', label: '购物车', component: markRaw(CartOutline) },
  { name: 'BagOutline', label: '购物袋', component: markRaw(BagOutline) },
  { name: 'WalletOutline', label: '钱包', component: markRaw(WalletOutline) },
  { name: 'CardOutline', label: '卡片', component: markRaw(CardOutline) },
  { name: 'CashOutline', label: '现金', component: markRaw(CashOutline) },
  {
    name: 'StatsChartOutline',
    label: '统计图',
    component: markRaw(StatsChartOutline),
  },
  {
    name: 'BarChartOutline',
    label: '柱状图',
    component: markRaw(BarChartOutline),
  },
  {
    name: 'PieChartOutline',
    label: '饼图',
    component: markRaw(PieChartOutline),
  },
  {
    name: 'TrendingUpOutline',
    label: '上升',
    component: markRaw(TrendingUpOutline),
  },
  {
    name: 'TrendingDownOutline',
    label: '下降',
    component: markRaw(TrendingDownOutline),
  },
  { name: 'PulseOutline', label: '脉冲', component: markRaw(PulseOutline) },
  {
    name: 'AnalyticsOutline',
    label: '分析',
    component: markRaw(AnalyticsOutline),
  },
  {
    name: 'SpeedometerOutline',
    label: '仪表盘',
    component: markRaw(SpeedometerOutline),
  },
  { name: 'ServerOutline', label: '服务器', component: markRaw(ServerOutline) },
  {
    name: 'HardwareChipOutline',
    label: '芯片',
    component: markRaw(HardwareChipOutline),
  },
  { name: 'DesktopOutline', label: '桌面', component: markRaw(DesktopOutline) },
  { name: 'LaptopOutline', label: '笔记本', component: markRaw(LaptopOutline) },
  {
    name: 'PhonePortraitOutline',
    label: '手机',
    component: markRaw(PhonePortraitOutline),
  },
  {
    name: 'TabletPortraitOutline',
    label: '平板',
    component: markRaw(TabletPortraitOutline),
  },
  { name: 'WatchOutline', label: '手表', component: markRaw(WatchOutline) },
  { name: 'TvOutline', label: '电视', component: markRaw(TvOutline) },
  { name: 'PrintOutline', label: '打印', component: markRaw(PrintOutline) },
  { name: 'ScanOutline', label: '扫描', component: markRaw(ScanOutline) },
  { name: 'QrCodeOutline', label: '二维码', component: markRaw(QrCodeOutline) },
  { name: 'BarcodeOutline', label: '条码', component: markRaw(BarcodeOutline) },
  { name: 'WifiOutline', label: 'WiFi', component: markRaw(WifiOutline) },
  {
    name: 'BluetoothOutline',
    label: '蓝牙',
    component: markRaw(BluetoothOutline),
  },
  { name: 'CloudOutline', label: '云', component: markRaw(CloudOutline) },
  { name: 'SunnyOutline', label: '晴天', component: markRaw(SunnyOutline) },
  { name: 'MoonOutline', label: '月亮', component: markRaw(MoonOutline) },
  {
    name: 'ThunderstormOutline',
    label: '雷暴',
    component: markRaw(ThunderstormOutline),
  },
  { name: 'RainyOutline', label: '下雨', component: markRaw(RainyOutline) },
  { name: 'SnowOutline', label: '下雪', component: markRaw(SnowOutline) },
  {
    name: 'ColorPaletteOutline',
    label: '调色板',
    component: markRaw(ColorPaletteOutline),
  },
  { name: 'BrushOutline', label: '画笔', component: markRaw(BrushOutline) },
  { name: 'BuildOutline', label: '构建', component: markRaw(BuildOutline) },
  { name: 'HammerOutline', label: '锤子', component: markRaw(HammerOutline) },
  {
    name: 'ConstructOutline',
    label: '施工',
    component: markRaw(ConstructOutline),
  },
  { name: 'CogOutline', label: '齿轮', component: markRaw(CogOutline) },
  { name: 'OptionsOutline', label: '选项', component: markRaw(OptionsOutline) },
  { name: 'ToggleOutline', label: '开关', component: markRaw(ToggleOutline) },
  {
    name: 'LockClosedOutline',
    label: '锁定',
    component: markRaw(LockClosedOutline),
  },
  {
    name: 'LockOpenOutline',
    label: '解锁',
    component: markRaw(LockOpenOutline),
  },
  { name: 'EyeOutline', label: '查看', component: markRaw(EyeOutline) },
  { name: 'EyeOffOutline', label: '隐藏', component: markRaw(EyeOffOutline) },
  { name: 'HelpOutline', label: '帮助', component: markRaw(HelpOutline) },
  {
    name: 'HelpCircleOutline',
    label: '帮助圆',
    component: markRaw(HelpCircleOutline),
  },
  {
    name: 'InformationCircleOutline',
    label: '信息',
    component: markRaw(InformationCircleOutline),
  },
  {
    name: 'AlertCircleOutline',
    label: '警告',
    component: markRaw(AlertCircleOutline),
  },
  {
    name: 'WarningOutline',
    label: '警告三角',
    component: markRaw(WarningOutline),
  },
  {
    name: 'CheckmarkCircleOutline',
    label: '成功',
    component: markRaw(CheckmarkCircleOutline),
  },
  {
    name: 'CloseCircleOutline',
    label: '关闭',
    component: markRaw(CloseCircleOutline),
  },
  {
    name: 'AddCircleOutline',
    label: '添加圆',
    component: markRaw(AddCircleOutline),
  },
  {
    name: 'RemoveCircleOutline',
    label: '移除圆',
    component: markRaw(RemoveCircleOutline),
  },
  { name: 'RefreshOutline', label: '刷新', component: markRaw(RefreshOutline) },
  {
    name: 'ReloadOutline',
    label: '重新加载',
    component: markRaw(ReloadOutline),
  },
  { name: 'SyncOutline', label: '同步', component: markRaw(SyncOutline) },
  {
    name: 'DownloadOutline',
    label: '下载',
    component: markRaw(DownloadOutline),
  },
  { name: 'LogInOutline', label: '登录', component: markRaw(LogInOutline) },
  { name: 'LogOutOutline', label: '登出', component: markRaw(LogOutOutline) },
  { name: 'EnterOutline', label: '进入', component: markRaw(EnterOutline) },
  { name: 'ExitOutline', label: '退出', component: markRaw(ExitOutline) },
  { name: 'ExpandOutline', label: '展开', component: markRaw(ExpandOutline) },
  {
    name: 'ContractOutline',
    label: '收缩',
    component: markRaw(ContractOutline),
  },
  { name: 'CopyOutline', label: '复制', component: markRaw(CopyOutline) },
  {
    name: 'ClipboardOutline',
    label: '剪贴板',
    component: markRaw(ClipboardOutline),
  },
  { name: 'CutOutline', label: '剪切', component: markRaw(CutOutline) },
  { name: 'SaveOutline', label: '保存', component: markRaw(SaveOutline) },
  { name: 'CodeOutline', label: '代码', component: markRaw(CodeOutline) },
  {
    name: 'CodeSlashOutline',
    label: '代码斜杠',
    component: markRaw(CodeSlashOutline),
  },
  {
    name: 'TerminalOutline',
    label: '终端',
    component: markRaw(TerminalOutline),
  },
  {
    name: 'GitBranchOutline',
    label: 'Git分支',
    component: markRaw(GitBranchOutline),
  },
  {
    name: 'GitCommitOutline',
    label: 'Git提交',
    component: markRaw(GitCommitOutline),
  },
  {
    name: 'GitMergeOutline',
    label: 'Git合并',
    component: markRaw(GitMergeOutline),
  },
  {
    name: 'GitPullRequestOutline',
    label: 'Git PR',
    component: markRaw(GitPullRequestOutline),
  },
  { name: 'BugOutline', label: 'Bug', component: markRaw(BugOutline) },
  { name: 'FlaskOutline', label: '实验', component: markRaw(FlaskOutline) },
  { name: 'RocketOutline', label: '火箭', component: markRaw(RocketOutline) },
  { name: 'FlashOutline', label: '闪电', component: markRaw(FlashOutline) },
  { name: 'BulbOutline', label: '灯泡', component: markRaw(BulbOutline) },
  {
    name: 'ExtensionPuzzleOutline',
    label: '扩展',
    component: markRaw(ExtensionPuzzleOutline),
  },
  { name: 'LayersOutline', label: '图层', component: markRaw(LayersOutline) },
  { name: 'CubeOutline', label: '立方体', component: markRaw(CubeOutline) },
  { name: 'ShapesOutline', label: '形状', component: markRaw(ShapesOutline) },
  { name: 'EllipseOutline', label: '椭圆', component: markRaw(EllipseOutline) },
  { name: 'SquareOutline', label: '方形', component: markRaw(SquareOutline) },
  {
    name: 'TriangleOutline',
    label: '三角形',
    component: markRaw(TriangleOutline),
  },
  {
    name: 'BusinessOutline',
    label: '商业',
    component: markRaw(BusinessOutline),
  },
  {
    name: 'StorefrontOutline',
    label: '店铺',
    component: markRaw(StorefrontOutline),
  },
  { name: 'SchoolOutline', label: '学校', component: markRaw(SchoolOutline) },
  {
    name: 'LibraryOutline',
    label: '图书馆',
    component: markRaw(LibraryOutline),
  },
  { name: 'MedkitOutline', label: '医疗', component: markRaw(MedkitOutline) },
  {
    name: 'RestaurantOutline',
    label: '餐厅',
    component: markRaw(RestaurantOutline),
  },
  { name: 'CafeOutline', label: '咖啡', component: markRaw(CafeOutline) },
  { name: 'LeafOutline', label: '树叶', component: markRaw(LeafOutline) },
  { name: 'FlowerOutline', label: '花朵', component: markRaw(FlowerOutline) },
  { name: 'PawOutline', label: '脚印', component: markRaw(PawOutline) },
  {
    name: 'FingerPrintOutline',
    label: '指纹',
    component: markRaw(FingerPrintOutline),
  },
  {
    name: 'ThumbsUpOutline',
    label: '点赞',
    component: markRaw(ThumbsUpOutline),
  },
  {
    name: 'ThumbsDownOutline',
    label: '踩',
    component: markRaw(ThumbsDownOutline),
  },
  { name: 'HappyOutline', label: '开心', component: markRaw(HappyOutline) },
  { name: 'SadOutline', label: '难过', component: markRaw(SadOutline) },
  {
    name: 'AccessibilityOutline',
    label: '辅助功能',
    component: markRaw(AccessibilityOutline),
  },
  { name: 'PlanetOutline', label: '星球', component: markRaw(PlanetOutline) },
  { name: 'EarthOutline', label: '地球', component: markRaw(EarthOutline) },
  {
    name: 'EllipsisHorizontalOutline',
    label: '更多(横)',
    component: markRaw(EllipsisHorizontalOutline),
  },
  {
    name: 'EllipsisVerticalOutline',
    label: '更多(竖)',
    component: markRaw(EllipsisVerticalOutline),
  },
  {
    name: 'ReorderFourOutline',
    label: '排序4',
    component: markRaw(ReorderFourOutline),
  },
  {
    name: 'ReorderThreeOutline',
    label: '排序3',
    component: markRaw(ReorderThreeOutline),
  },
  {
    name: 'ReorderTwoOutline',
    label: '排序2',
    component: markRaw(ReorderTwoOutline),
  },
];

// 过滤图标
const filteredIcons = computed(() => {
  if (!searchText.value) return iconList;
  const keyword = searchText.value.toLowerCase();
  return iconList.filter(
    (icon) =>
      icon.name.toLowerCase().includes(keyword) || icon.label.includes(keyword),
  );
});

// 图标映射
const iconMap: Record<string, Component> = {};
iconList.forEach((icon) => {
  iconMap[icon.name] = icon.component;
});

// 获取图标组件
function getIconComponent(name: string): Component | undefined {
  return iconMap[name];
}

// 选择图标
function handleSelect(name: string) {
  modelValue.value = name;
}

// 清除
function handleClear() {
  modelValue.value = '';
}

// 导出获取图标方法供外部使用
defineExpose({ getIconComponent, iconMap });
</script>

<template>
  <Popover>
    <PopoverTrigger as-child>
      <div class="relative">
        <Input
          :model-value="modelValue"
          placeholder="请选择图标"
          readonly
          class="cursor-pointer"
          :class="modelValue ? 'px-8' : undefined"
        />
        <span
          v-if="modelValue"
          class="pointer-events-none absolute top-1/2 left-2 inline-flex -translate-y-1/2 text-[18px]"
        >
          <component :is="getIconComponent(modelValue)" />
        </span>
        <button
          v-if="modelValue"
          type="button"
          class="absolute top-1/2 right-2 inline-flex -translate-y-1/2 text-sm"
          @click.stop="handleClear"
        >
          <CloseOutline />
        </button>
      </div>
    </PopoverTrigger>
    <PopoverContent
      class="w-[400px] p-3"
      align="start"
      side="bottom"
    >
      <Input
        v-model="searchText"
        placeholder="搜索图标"
        class="mb-3 h-8"
      />
      <div class="max-h-[300px] overflow-y-auto">
        <div class="grid grid-cols-6 gap-2">
          <button
            v-for="icon in filteredIcons"
            :key="icon.name"
            type="button"
            class="hover:bg-accent flex cursor-pointer flex-col items-center justify-center rounded-md border border-transparent px-1 py-2"
            :class="
              modelValue === icon.name
                ? 'border-primary bg-accent text-primary'
                : undefined
            "
            :title="icon.name"
            @click="handleSelect(icon.name)"
          >
            <span class="inline-flex text-[22px]">
              <component :is="icon.component" />
            </span>
            <span class="mt-1 max-w-full truncate text-center text-[10px]">
              {{ icon.label }}
            </span>
          </button>
        </div>
      </div>
    </PopoverContent>
  </Popover>
</template>
