# 18 / 3-1：读懂拼团页面，再改两处模拟交互

> 承接 2-14：后端已经有锁单、结算和通知。现在先搭一个能看、能点的页面，后面再接接口。
> 对应[原文第 18 节](../orgin-tutorial/3.第3部分：外部对接「静态页面」/18-3-1-DeepSeek设计拼团UI.md)，参考分支 `3-1-250202-xfg-ui`。按你选择的方式：**读参考页面，做小改动，不重抄样式。** 不要求注册 DeepSeek 或调用模型 API。

## 0. 这节只做什么

```text
打开商品页 → 点击购买 → 未模拟登录则跳登录页
→ 输入演示用户 → 返回商品页 → 显示支付弹窗 → 关闭模拟弹窗
```

**不调用后端，不生成订单，不触发结算，不扫码或付款。** 页面价格、团员、人数、倒计时都是演示数据，不能当数据库事实。

你将学会：区分 HTML/CSS/JS；追踪按钮事件；理解“浏览器记住一个用户名”为什么不等于真正登录。上节 Java 测试基线为用户 11 项＋项目外补充 3 项；本节不增加 Java 测试，改用浏览器交互验证，不把点击次数计为 JUnit 数量。

## 1. 我自己做，会先想什么？

先别看所有样式。一个页面只有三类问题：

| 问题 | 文件/概念 | 对照 Java 理解 |
| --- | --- | --- |
| 页面有哪些东西？ | HTML：标题、图片、按钮、表单 | 类似声明对象的结构，不负责业务判断 |
| 放在哪里、长什么样？ | CSS：颜色、间距、布局 | 展示规则，不决定是否真的成团 |
| 点了以后做什么？ | JavaScript：监听事件、判断、跳转 | 像给按钮注册回调方法 |

本节参考增量只有静态文件及设计稿，没有新增 Java 接口。原课用 AI 生成 UI，我们复用它的结果、读懂关键部分；**你没有调用 DeepSeek，也不要在简历上写自己使用了它。**

## 2. 先拿到正确版本的页面

你的当前工程还没有 `docs/dev-ops/nginx/html`。不要从参考仓库当前打开的页面直接复制：它可能不是本节版本。

### 2.1 只读导出指定分支

在 IDEA 底部 Terminal 选择 **Git Bash**，确认位于当前工程根目录，执行：

```bash
git -C reference/group-buy-market archive --format=zip --output="C:/Users/29150/AppData/Local/Temp/group-buy-ui-3-1.zip" 3-1-250202-xfg-ui docs/dev-ops/nginx/html
```

若该 ZIP 已存在，先换一个未使用的文件名，别覆盖自己已有资料。命令只读参考 Git 对象，不切分支，不改参考文件。

1. 用资源管理器打开上述 ZIP，解压到项目外临时目录。
2. 打开解压结果中的 `docs/dev-ops/nginx`。
3. 将整个 `nginx` 文件夹复制到**你自己工程的** `docs/dev-ops` 下。若目标已经存在，先停下核对，不覆盖现有页面。
4. 回到 IDEA，应该看到：

```text
docs/dev-ops/nginx/html/
  index.html          商品页和支付弹窗
  login.html          登录页
  css/index.css       商品页样式
  css/login.css       登录页样式
  js/index.js         购买按钮、弹窗、轮播和倒计时
  js/login.js         表单提交与模拟登录
  images/...          本地展示图片
```

静态文件不是 app 的 Java 资源，本节不改 Maven、Spring、SQL，也不需要启动 Application 或 Nginx。

### 2.2 用 IDEA 打开，不直接双击 file://

1. 在 IDEA 打开 `index.html`。
2. 用编辑器右上角浏览器图标，或右键 **Open in → Browser → Chrome/Edge**。
3. 检查地址栏是 `http://localhost:...` 或 `http://127.0.0.1:...`，而不是 `file:///...`。本节 Cookie 依赖 HTTP 页面，直接文件打开的行为可能不同。
4. 按 F12 打开开发者工具，点击设备工具栏图标，选择手机宽度（例如 390 像素）。没有设备图标也可以先用普通窗口。

若 IDEA 没有 HTML 浏览器预览功能，告诉我具体提示；先不安装前端框架或插件。若有本机 Python，可在 Git Bash 使用这个仅本机静态服务作为替代：

```bash
python -m http.server 18731 --bind 127.0.0.1 --directory docs/dev-ops/nginx/html
```

打开 `http://127.0.0.1:18731/index.html`；结束时在这个终端按 Ctrl+C。若端口占用先换未用端口，不终止其他程序。全程保持同一个主机名和端口，避免 Cookie 观察不一致。

## 3. 在 IDEA 只追一条点击链

按照这个顺序找，不用逐行读 CSS：

1. `index.html` 搜索 `group-buy`：这是“开团购买”按钮，`data-price="40"` 是它携带的演示金额。
2. 搜索 `paymentModal`：弹窗本来就在 HTML 里，只是 CSS 默认隐藏；不是点击后又向后台下载一个页面。
3. 滚到文件底部：`<script src="js/index.js">` 把行为代码接进来。
4. 打开 `js/index.js`，找 `querySelectorAll(".group-btn, .action-btn")`：一次取出参团和购买按钮。
5. 看 `addEventListener("click", ...)`：给按钮注册点击回调。里面先读用户名 Cookie；没找到就设置 `window.location.href = "login.html"` 并 return。
6. 找 `showPaymentModal(price)`：它只改 `textContent` 和 `style.display`，**没有 HTTP 锁单**。
7. 打开 `js/login.js`：原代码读取密码，却没有核验密码；提交后直接写 Cookie，再跳首页。

三个术语够用：`document` 是当前页面；DOM 是浏览器把 HTML 变成的可操作对象；事件监听就是“发生点击时，调用这个函数”。`DOMContentLoaded` 表示 HTML 结构已解析，可以按 id 找元素。

**先预测**：浏览器刷新后用户名还在吗？看到“支付成功”会改变 `complete_count` 吗？带着这两个问题再做下面练习。

## 4. 两个小改动：让演示页面不再冒充真实系统

### 改动一：承认这只是模拟登录，不索取密码

为什么改：原版任意密码都会通过，容易让你把“写 Cookie”误认成认证。我们保留用户名演示，把密码框移除，并明确标注模拟。

先替换 HTML，再替换 JS；两者使用同样的 `loginForm / username / errorMessage`，不用改 CSS。

**文件**：`docs/dev-ops/nginx/html/login.html`

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>模拟登录 · 拼团演示</title>
    <link rel="stylesheet" href="css/login.css">
</head>
<body>
<div class="container">
    <form id="loginForm" class="login-form">
        <h2>模拟登录</h2>
        <p>只用于页面演示，不验证身份，请勿填写真实密码。</p>
        <div class="input-group">
            <!-- 保留原 id，让现有样式与脚本仍能定位输入框。 -->
            <input type="text" id="username" required maxlength="32">
            <label for="username">演示用户名</label>
        </div>
        <button type="submit">进入演示</button>
        <p class="error-message" id="errorMessage" role="alert"></p>
    </form>
</div>
<script src="js/login.js"></script>
</body>
</html>
```

**文件**：`docs/dev-ops/nginx/html/js/login.js`

```javascript
document.addEventListener("DOMContentLoaded", () => {
  const form = document.getElementById("loginForm")
  const error = document.getElementById("errorMessage")

  form.addEventListener("submit", (event) => {
    // 不让浏览器按默认表单提交刷新；本节没有登录接口。
    event.preventDefault()
    const username = document.getElementById("username").value.trim()
    if (!username) {
      error.textContent = "请输入演示用户名，不能只填空格"
      error.style.display = "block"
      return
    }

    // 保留 username 这个 Cookie 名，商品页原有 getCookie 才能读到。
    // 编码能避免分号等字符破坏 Cookie 格式，不等于加密或身份认证。
    document.cookie = `username=${encodeURIComponent(username)}; Max-Age=86400; Path=/; SameSite=Lax`
    window.location.href = "index.html"
  })
})
```

`trim()` 去掉首尾空格；`preventDefault()` 阻止默认提交。Cookie 保留一天，所以刷新后还能被读到。用户能自己改 Cookie，**它不能作为后端权限凭据**；不存真实密码，也不拿它保证支付安全。

### 改动二：支付按钮明确是演示，不展示可扫描二维码

为什么改：这节还没支付接口。一个弹窗加成功提示不能说明付过钱。

在 `index.html` 的 `paymentModal` 内，只改这三处，其他结构保留：

```text
“请扫码支付” → “模拟支付（不扣款）”
二维码 img 整行 → 下方的 p 元素
“支付完成”按钮文字 → “完成演示”，保留 id="completePayment"
```

替代二维码的完整元素：

```html
<p>本节只演示交互，没有支付二维码，也不会创建或结算订单。</p>
```

在 `js/index.js` 找原来的 `completePayment.addEventListener`，只替换它，不动轮播与倒计时：

```javascript
completePayment.addEventListener("click", () => {
  // 弹窗关闭仅代表这次页面演示结束，不调用营销结算服务。
  alert("模拟完成：未扣款，未创建或结算订单")
  hidePaymentModal()
})
```

这里仍是原有流程的小修，不加入真实支付、登录服务或后端接口。弹窗金额仍来自 `data-price`，用户可以改它，**以后真正应付金额必须由服务端计算与核验**。

## 5. 按步骤验证，不用猜要测什么

先保存三个文件和 JS 的局部修改；刷新商品页。遇到旧样式/脚本，开发者工具打开时用浏览器的强制刷新，不要清空整个浏览器所有网站的数据。

| 操作顺序 | 应看到什么 | 这一步说明什么 |
| --- | --- | --- |
| 1. 用一个新的无痕窗口，打开同一个 HTTP 地址，点开团购买 | 跳到模拟登录页，没有密码框 | 用户名尚未记录，购买回调走未登录分支 |
| 2. 用户名只填空格，点进入演示 | 原页显示“不能只填空格” | trim 后为空，没有写入 Cookie 并跳转 |
| 3. 输入 `learner01`，提交 | 回到商品页 | 浏览器写入模拟用户名，JS 主动跳转 |
| 4. 再点开团购买 | 显示￥40，标题含“不扣款”，不显示二维码 | 同一浏览器读到了 Cookie，金额来自按钮 |
| 5. 点取消支付 | 弹窗消失 | 只改变可见性 |
| 6. 点单独购买 | 显示￥100 | 同一函数处理不同按钮的演示金额 |
| 7. 点完成演示，再确认提示框 | 提示未扣款，弹窗关闭 | 只是本地事件，不是后端结算 |
| 8. 刷新，再点开团购买 | 不要求重新模拟登录 | Cookie 跨刷新保留，不是局部变量 |

如果之前测试过留下 Cookie，不用全局清数据；新无痕窗口会提供独立的新会话。若已有无痕窗口，先关闭你自己的该组无痕窗口再重新打开，以免共享已有会话。不要在开发者工具清理不相关站点。

再做两次定位：

- F12 → **Application（或应用）→ Cookies → 当前站点**，看 `username`；某些浏览器把 Cookie 放在 Storage/存储下。不用修改它。
- F12 → **Network（网络）→ Fetch/XHR**，点击购买、完成演示。本节应无营销请求；图片、HTML、JS 的加载不是营销接口调用。若出现接口请求，先核对是否复制了后续分支。

### 失败时先看哪里

- 图片全不见：看复制的 `images` 是否齐全，不把远程图床故障当后端错误。
- `Cannot read properties of null`：检查 HTML 的 id 与脚本是否一致，是否删错元素。
- Cookie 写不进或一直回登录：检查不是 file://、没有换主机名，浏览器是否禁止 Cookie。
- 输入空格后仍跳转：确认保存并刷新了修改后的 login.js，而不是另一份文件。
- 点击按钮没反应：先看 Console 的第一条红色错误，把错误和文件名发我；不要同时改 Java、数据库和 Nginx 排查。

## 6. 小结和验收

回看参考版本：我们保留同样的商品页、登录页、购买弹窗和目录布局；没有还原全部 UI 细节，也没有引入新框架。主动去掉虚假的密码认证与扫码暗示，其他轮播/倒计时仅跟读。

你只需要交回：

1. 一张修改后的模拟登录页、一张打开的模拟支付弹窗截图，以及上表哪些步骤没有达到预期。
2. 口述：为什么“用户名 Cookie 存在”不等于认证通过？
3. 口述：为什么点了“完成演示”后，数据库不会增加 `complete_count`？

验收通过后先停下来，由你提交 Git，再确认继续。作者下一节是 **19 / 2-15《根据 UI 展示封装接口》**：这时再让页面需要的数据反过来推动后端接口设计，不在本节提前接入。

## 7. 备课验证记录

- 已确认用户提交 `33a9591`，本轮起始工作区干净。
- 已核对 2-14 → `3-1-250202-xfg-ui` 增量：11 个新增静态/设计稿文件，无 Java 增量。参考仓库未修改或切分支。
- 已在项目外导出指定分支，通过仅本机 HTTP 服务在真实浏览器观察：未登录购买跳转；原版任意演示密码写入用户名；开团弹窗显示￥40。未调用业务后端。
- 从本文抽取 2 个完整文件，并把弹窗文案、二维码替换与事件片段应用到项目外同一副本。真实浏览器已验证：密码框移除；空白用户名留在登录页并提示；有效用户名跳转首页；开团金额40、单购100；取消隐藏弹窗；“完成演示”提示未扣款且关闭；刷新后模拟用户名仍生效。资源记录只出现 HTML/图片/CSS/JS等静态资源，未见 Fetch/XHR 营销请求。这是浏览器行为验证，不是 Java 测试条数。
- 破坏实验仅在项目外移除 `.trim()`，空格用户名实际错误跳转首页、Cookie 变为 `%20%20%20`；还原后空格输入重新被拦截。随后有效用户名仍能正常跳转。恢复的两个整文件与本文代码块核对一致。
- 验证中曾因浏览器缓存看到原文案，强制重载后确认新版本；另外等待默认隐藏弹窗“可见”导致超时，改为先点击再观察。本轮最终浏览器会话已结束，项目外临时静态服务已停止。
- 本节未运行 Maven、Java 测试、真实登录/支付或 DB/Redis；不将历史通过计为本节实测。用户页面文件由用户操作，本轮只写教程和进度。
