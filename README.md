# PvPAreaPlugin

A Paper plugin that turns selected regions into PvP zones with persistent kill / death stats, action bar HUDs, kill effects, and a top-killer leaderboard (chat + holograms).

指定した領域を PvP ゾーンに変え、永続的なキル/デス統計、アクションバー HUD、キル時のエフェクト、キルランキング（チャット + ホログラム）を提供する Paper プラグインです。

---

## English

### Features

- Multi-region PvP areas selected with a Golden Axe
- Persistent kills / deaths / K-D ratio per player
- Action bar HUD shown only while inside a PvP area
- Configurable sound + particle effects on kill
- `/killtop` command and top-killer holograms via [FancyHolograms](https://modrinth.com/plugin/fancyholograms)
- Fully configurable messages (MiniMessage)
- Pluggable storage backend: **SQLite** (default) or **YAML**
- Async periodic saves and debounced hologram refresh for low tick cost
- `/pvparea reload` for hot config changes

### Requirements

- Paper 1.21.4+
- Java 21
- [FancyHolograms](https://modrinth.com/plugin/fancyholograms) (hard dependency)
- `sqlite-jdbc` is auto-downloaded at runtime via Paper's library loader — no shading required.

### Installation

1. Drop `PvPAreaPlugin-x.y.z.jar` into `plugins/`.
2. Start the server once to generate `plugins/PvPAreaPlugin/config.yml`.
3. Edit `config.yml` and run `/pvparea reload`.

### Commands

| Command | Description | Permission |
|---|---|---|
| `/pvparea create <name>` | Create a PvP area from your current selection | `pvparea.admin` |
| `/pvparea remove <name>` | Delete a PvP area | `pvparea.admin` |
| `/pvparea hologram create <name>` | Spawn a kill-top hologram at your location | `pvparea.admin` |
| `/pvparea hologram remove <name>` | Remove a kill-top hologram | `pvparea.admin` |
| `/pvparea reload` | Reload `config.yml` | `pvparea.admin` |
| `/killtop` | Print the top killers in chat | `pvparea.use` |

### Selecting a region

Hold a **Golden Axe**, left-click a corner to set pos1, right-click the opposite corner to set pos2, then run `/pvparea create <name>`.

### Configuration

See [`src/main/resources/config.yml`](src/main/resources/config.yml) for the full file. All strings use [MiniMessage](https://docs.advntr.dev/minimessage/) formatting.

#### Storage

```yaml
storage:
  type: sqlite          # or "yaml"
  save-interval-ticks: 6000   # 5 minutes
```

- **sqlite** (default): stored at `plugins/PvPAreaPlugin/stats.db` in WAL mode. Recommended once the player base grows.
- **yaml**: stored at `plugins/PvPAreaPlugin/stats.yml`. Fine for small servers.

Stats writes use a dirty flag and are flushed asynchronously on an interval, so kills never cause disk I/O on the main thread.

> Changing `storage.type` requires a full server restart (not a `/pvparea reload`).

#### Kill effects

```yaml
kill-effects:
  sound:
    enabled: true
    name: ENTITY_PLAYER_LEVELUP
    volume: 1.0
    pitch: 1.2
  particles:
    enabled: true
    type: CRIT
    count: 12
    offset-x: 0.3
    offset-y: 0.6
    offset-z: 0.3
    speed: 0.05
```

`name` accepts any Bukkit `Sound` enum value. `type` accepts any Bukkit `Particle` enum value.

#### Placeholders

| Key | Placeholders |
|---|---|
| `actionbar.format` | `{kills}` `{deaths}` `{kd}` `{ping}` |
| `killtop.line` | `{rank}` `{name}` `{kills}` |
| `messages.area-created` / `area-removed` / `hologram-created` | `{name}` |
| `messages.pos1-set` / `pos2-set` | `{x}` `{y}` `{z}` |

### Building

```bash
mvn clean package
```

Output jar: `target/PvPAreaPlugin-<version>.jar`

---

## 日本語

### 機能

- Golden Axe で範囲選択できる複数 PvP エリア
- プレイヤーごとの永続的な Kills / Deaths / K/D 管理
- PvP エリア内のみ表示されるアクションバー HUD
- キル時の効果音・パーティクルを config で設定可能
- `/killtop` コマンド & [FancyHolograms](https://modrinth.com/plugin/fancyholograms) によるキルランキングホログラム
- すべてのメッセージを MiniMessage で自由にカスタマイズ
- ストレージを **SQLite**(デフォルト) または **YAML** から選択可能
- 非同期・間引き保存でメインスレッドに負荷をかけない最適化
- `/pvparea reload` による設定のホットリロード

### 必要環境

- Paper 1.21.4 以降
- Java 21
- [FancyHolograms](https://modrinth.com/plugin/fancyholograms)（ホログラム使用時に必須）
- `sqlite-jdbc` は Paper の library-loader により起動時に自動ダウンロードされます

### インストール

1. `PvPAreaPlugin-x.y.z.jar` を `plugins/` フォルダに配置
2. サーバーを 1 度起動して `plugins/PvPAreaPlugin/config.yml` を生成
3. `config.yml` を編集してから `/pvparea reload` を実行

### コマンド一覧

| コマンド | 説明 | 権限 |
|---|---|---|
| `/pvparea create <名前>` | 選択範囲から PvP エリアを作成 | `pvparea.admin` |
| `/pvparea remove <名前>` | PvP エリアを削除 | `pvparea.admin` |
| `/pvparea hologram create <名前>` | 現在地にキルランキングホログラムを設置 | `pvparea.admin` |
| `/pvparea hologram remove <名前>` | ホログラムを削除 | `pvparea.admin` |
| `/pvparea reload` | `config.yml` をリロード | `pvparea.admin` |
| `/killtop` | キルランキングをチャットに表示 | `pvparea.use` |

### エリアの選択方法

**金の斧 (Golden Axe)** を持って、左クリックで pos1、右クリックで pos2 を指定し、`/pvparea create <名前>` を実行してください。

### 設定

詳細は [`src/main/resources/config.yml`](src/main/resources/config.yml) を参照してください。すべての文字列は [MiniMessage](https://docs.advntr.dev/minimessage/) 形式です。

#### ストレージ

```yaml
storage:
  type: sqlite          # または "yaml"
  save-interval-ticks: 6000   # 5 分ごと
```

- **sqlite**（推奨）: `plugins/PvPAreaPlugin/stats.db` に WAL モードで保存。プレイヤー数が増えるほど有利です。
- **yaml**: `plugins/PvPAreaPlugin/stats.yml` に保存。小規模サーバー向け。

データは dirty フラグで管理され、一定間隔で非同期に書き込まれるため、キル時にメインスレッドでディスク I/O が発生しません。

> `storage.type` の変更は `/pvparea reload` では反映されません。サーバーを再起動してください。

#### キル時エフェクト

```yaml
kill-effects:
  sound:
    enabled: true
    name: ENTITY_PLAYER_LEVELUP
    volume: 1.0
    pitch: 1.2
  particles:
    enabled: true
    type: CRIT
    count: 12
    offset-x: 0.3
    offset-y: 0.6
    offset-z: 0.3
    speed: 0.05
```

`name` には Bukkit の `Sound` enum の値、`type` には `Particle` enum の値が指定できます。

#### プレースホルダ

| 設定キー | 使用可能なプレースホルダ |
|---|---|
| `actionbar.format` | `{kills}` `{deaths}` `{kd}` `{ping}` |
| `killtop.line` | `{rank}` `{name}` `{kills}` |
| `messages.area-created` / `area-removed` / `hologram-created` | `{name}` |
| `messages.pos1-set` / `pos2-set` | `{x}` `{y}` `{z}` |

### ビルド

```bash
mvn clean package
```

出力: `target/PvPAreaPlugin-<version>.jar`

---

## License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.

本プロジェクトは **MIT License** の下で公開されています。詳細は [LICENSE](LICENSE) を参照してください。
