# Exposure Grading — 曝光评级

A NeoForge mod for Minecraft 1.21.1 that automatically grades photographs from the [Exposure](https://github.com/mortuusars/Exposure) mod using the GLM-4V API, with star ratings displayed via tooltip.

为 Minecraft 1.21.1 的 NeoForge 模组，调用 GLM-4V API 自动评价 [Exposure](https://github.com/mortuusars/Exposure) 拍摄的照片，以五角星 tooltip 显示评分结果。

## Features / 功能

- **AI-powered photo grading** — evaluates composition, tone, creativity, and content via GLM-4V-Flash API
- **Star rating display** — 0-5★ tooltip on rated photographs
- **Weighted total score** — configurable per-dimension weights, computed server-side
- **Atomic rating flow** — state auto-recovers on game exit / crash (120s timeout)
- **Full multiplayer support** — client→server→API→write rating via C2S packet
- **Configurable model** — switch between `glm-4v-flash`, `glm-4.6v-flash`, or any compatible model in TOML config

---

- **AI 照片评分** — 通过 GLM-4V-Flash API 从构图、影调、创意、内容四维度评价
- **星级展示** — 已评分照片 tooltip 显示 0-5★
- **加权总分** — 各维度权重可在服务端配置，总分服务端计算
- **原子化流程** — 退出游戏/崩溃后评分状态自动恢复（120 秒超时）
- **完整多人支持** — C2S 数据包流程：客户端→服务端→API→评分写入
- **可配置模型** — 在 TOML 配置中切换 `glm-4v-flash`、`glm-4.6v-flash` 等兼容模型

## Requirements / 环境要求

- Minecraft 1.21.1
- NeoForge 21.1.136
- [Exposure](https://github.com/mortuusars/Exposure) 1.9.18 (required)
- Polaroid 1.1.5 (optional/soft dependency)

## Installation / 安装

1. Install NeoForge 21.1.136 for Minecraft 1.21.1
2. Place `exposure-1.21.1-neoforge-1.9.18.jar` into `mods/`
3. Place `exposure_grading-1.0.0.jar` into `mods/`
4. Launch game, then configure `config/exposure_grading-common.toml`

## Configuration / 配置

`exposure_grading-common.toml`:

```toml
[server]
    apiKey = "your-api-key"
    apiUrl = "https://open.bigmodel.cn/api/paas/v4/chat/completions"
    modelName = "glm-4v-flash"
    weightComposition = 1.0
    weightTone = 1.0
    weightCreativity = 1.0
    weightContent = 1.0
```

- **apiKey** — 智谱 GLM API key（必须实名认证）
- **apiUrl** — API 端点 URL
- **modelName** — 模型名，支持 `glm-4v-flash`、`glm-4.6v-flash` 等
- **weightXxx** — 各维度权重（0.0 ~ 1.0），总分 = 加权平均

## Usage / 使用

1. Craft a **Review Table** (评审台)
2. Place a photograph in the slot
3. Click **Rate** (评分) button
4. Wait for the AI rating (a few seconds)
5. Hover over the photograph to see the star rating in tooltip

## Data Component / 数据组件

Rated photographs carry `exposure_grading:photo_rating`:

```json
{
    "composition": 6.0,
    "tone": 7.2,
    "creativity": 5.0,
    "content": 5.5,
    "totalScore": 5.925,
    "comment": "评语"
}
```

Accessible via KubeJS: `item.nbt.getCompound('exposure_grading:photo_rating').getDouble('totalScore')`

## License / 许可

MIT
