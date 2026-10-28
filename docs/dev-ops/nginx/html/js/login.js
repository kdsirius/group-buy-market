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