# CalendarView

基于 [huanghaibin-dev/CalendarView](https://github.com/huanghaibin-dev/CalendarView) `v3.7.1` 魔改的 Android 日历控件库，
通过 JitPack 发布：`com.github.kevinvane:CalendarView:3.7.1.x`。

## Modules

| 模块 | 类型 | 是否参与构建 | 说明 |
| --- | --- | --- | --- |
| `calendarview` | `com.android.library` | ✅ | **核心库**。全部实现代码位于 `com.haibin.calendarview` 单包下，minSdk 23 / compileSdk 34，`versionName = 3.7.1.5`。改动实现时只需关注此模块。 |
| `app2` | `com.android.application` | ✅ | **示例 Demo**，也是当前唯一的可运行验证入口。包名 `com.demo.calendar`，仅含 `MainActivity` 与一套黑色自定义样式（`SimpleBlackMonthView` / `SimpleBlackWeekBar` / `SimpleBlackWeekView`），用于演示 `CalendarView` + `setSchemeDate` 的用法。 |
| `app` | `com.android.application` | ❌ 已弃用 | 上游原版 Demo，包名 `com.haibin.calendarviewproject`，收录了十余种主题样式（simple / meizu / colorful / range / multi / progress / solar / group / index / mix 等）。`settings.gradle` 中已注释掉 `include ':app'`，不参与编译；其中还依赖 glide 等本项目不再使用的库。仅作参考，不要在此修改。 |

## Notes

- `settings.gradle` 当前只 `include ':calendarview'` 与 `include ':app2'`；新增模块需同步登记。
- 库的对外入口与可替换插槽：
  - 入口/容器：`CalendarView`、`CalendarLayout`
  - 主题配置：`CalendarViewDelegate`、`Calendar`
  - 可继承替换的 UI：`BaseMonthView` / `BaseWeekView` / `BaseView`、`MonthView` / `WeekView` / `YearView` / `WeekBar`、`RangeMonthView` / `RangeWeekView` / `MultiMonthView` / `MultiWeekView`
  - Pager：`MonthViewPager`、`WeekViewPager`、`YearViewPager`
  - 工具：`CalendarUtil`、`LunarCalendar`、`LunarUtil`、`SolarTermUtil`、`TrunkBranchAnnals`
- 使用文档见 `QUESTION.md`（英文）与 `QUESTION_ZH.md`（中文）；README 中引用的截图取自 `app2/preview/`。
- 依赖仓库统一走阿里云镜像（`build.gradle`），如需引入新依赖请确认镜像已覆盖。
- 构建：`./gradlew :calendarview:assembleRelease`（库）、`./gradlew :app2:assembleDebug`（Demo）。
