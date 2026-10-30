const BASE_URL = 'http://127.0.0.1:8091'
const SOURCE = 's01'
const CHANNEL = 'c01'
const GOODS_ID = '9890001'

// 当前页面持有的模拟交易号；锁单成功后才赋值，结算时必须复用它。
let currentOutTradeNo = null

// 页面加载后先拿后端数据，再绑定动态生成的按钮。
document.addEventListener('DOMContentLoaded', async () => {
  const userId = getCookie('username')
  if (!userId) {
    window.location.href = 'login.html'
    return
  }

  bindStaticEvents(userId)
  await loadMarketConfig(userId)
})

async function loadMarketConfig(userId) {
  try {
    const response = await fetch(`${BASE_URL}/api/v1/gbm/index/query_group_buy_market_config`, {
      method: 'POST',
      headers: {'Content-Type': 'application/json'},
      body: JSON.stringify({userId, source: SOURCE, channel: CHANNEL, goodsId: GOODS_ID})
    })
    const result = await response.json()
    if (result.code !== '0000') {
      showPageError(`首页查询失败：${result.info || result.code}`)
      return
    }

    renderMarketConfig(result.data, userId)
  } catch (error) {
    console.error('首页查询请求失败', error)
    showPageError('首页查询失败，请确认后端服务已启动')
  }
}

function renderMarketConfig(data, userId) {
  const {goods, teamList = [], teamStatistic} = data
  document.querySelector('.promotion-text').textContent =
    `直降 ¥${goods.deductionPrice.toFixed(0)}，${teamStatistic.allTeamUserCount}人再抢，参与马上抢到`

  const groupList = document.querySelector('.group-list')
  groupList.innerHTML = ''

  if (teamList.length === 0) {
    groupList.innerHTML = '<div class="group-item empty-tips"><div class="tips-content">小伙伴，赶紧去开团吧！</div></div>'
  } else {
    teamList.forEach(team => {
      const remaining = team.targetCount - team.lockCount
      const item = document.createElement('div')
      item.className = 'group-item'
      item.innerHTML = `
        <div>
          <div class="user-info" data-team-id="${escapeHtml(team.teamId)}">${escapeHtml(team.userId)}</div>
          <div class="group-status">
            <span>组队仅剩${remaining}人，拼单即将结束</span>
            <span class="countdown">${escapeHtml(team.validTimeCountdown)}</span>
          </div>
        </div>
        <div class="right"><button class="group-btn" data-price="${goods.payPrice}" data-team-id="${escapeHtml(team.teamId)}">参与拼团</button></div>`
      groupList.appendChild(item)
    })
    document.querySelectorAll('.countdown').forEach(element => new Countdown(element, element.textContent))
  }

  document.querySelector('.buy-alone').textContent = `单独购买(￥${goods.originalPrice.toFixed(0)})`
  document.querySelector('.group-buy').textContent = `开团购买(￥${goods.payPrice.toFixed(0)})`
  document.querySelector('.buy-alone').dataset.price = goods.originalPrice
  document.querySelector('.group-buy').dataset.price = goods.payPrice

  // 只有自己的团明细才携带 outTradeNo，其他人的单号不进入页面。
  const ownTeam = teamList.find(team => team.userId === userId && team.outTradeNo)
  currentOutTradeNo = ownTeam ? ownTeam.outTradeNo : null
  document.querySelectorAll('.group-btn').forEach(button => {
    button.addEventListener('click', () => lockOrder(userId, button.dataset.teamId, button.dataset.price))
  })
}

async function lockOrder(userId, teamId, price) {
  // 已有未支付单时复用同一外部单号，避免重复锁单；否则生成新单号。
  const outTradeNo = currentOutTradeNo || generateTradeNo()
  try {
    const response = await fetch(`${BASE_URL}/api/v1/gbm/trade/lock_market_pay_order`, {
      method: 'POST',
      headers: {'Content-Type': 'application/json'},
      body: JSON.stringify({userId, teamId: teamId || null, activityId: 100123, goodsId: GOODS_ID,
        source: SOURCE, channel: CHANNEL, outTradeNo, notifyUrl: `${BASE_URL}/api/v1/test/group_buy_notify`})
    })
    const result = await response.json()
    if (result.code !== '0000') {
      showPageError(`锁单失败：${result.info || result.code}`)
      return
    }

    currentOutTradeNo = outTradeNo
    showPaymentModal(price)
  } catch (error) {
    console.error('锁单请求失败', error)
    showPageError('锁单失败，请检查后端服务')
  }
}

function bindStaticEvents(userId) {
  document.querySelector('.group-buy').addEventListener('click', () => lockOrder(userId, null, document.querySelector('.group-buy').dataset.price || ''))
  document.querySelector('.buy-alone').addEventListener('click', () => showPageError('本节只演示拼团锁单，单独购买暂未接入'))
  document.getElementById('cancelPayment').addEventListener('click', hidePaymentModal)
  document.getElementById('completePayment').addEventListener('click', () => settleOrder(userId))
  window.addEventListener('click', event => { if (event.target === document.getElementById('paymentModal')) hidePaymentModal() })
}

async function settleOrder(userId) {
  if (!currentOutTradeNo) return showPageError('缺少外部交易单号，不能结算')
  try {
    const response = await fetch(`${BASE_URL}/api/v1/gbm/trade/settlement_market_pay_order`, {
      method: 'POST', headers: {'Content-Type': 'application/json'},
      body: JSON.stringify({userId, source: SOURCE, channel: CHANNEL,
        outTradeNo: currentOutTradeNo, outTradeTime: Date.now()})
    })
    const result = await response.json()
    if (result.code !== '0000') {
      showPageError(`结算失败：${result.info || result.code}`)
      return
    }
    alert('支付&结算成功')
    window.location.reload()
  } catch (error) {
    console.error('结算请求失败', error)
    showPageError('结算失败，请检查后端服务')
  } finally {
    hidePaymentModal()
  }
}

function showPaymentModal(price) {
  document.getElementById('paymentAmount').textContent = `支付金额：￥${Number(price).toFixed(2)}`
  document.getElementById('outTradeNo').textContent = `交易号：${currentOutTradeNo}`
  document.getElementById('paymentModal').style.display = 'block'
}

function hidePaymentModal() { document.getElementById('paymentModal').style.display = 'none' }
function showPageError(message) { const error = document.getElementById('pageError'); error.textContent = message; error.style.display = 'block' }
function generateTradeNo() { return `${Date.now()}${Math.floor(Math.random() * 1000)}`.slice(-12) }
function getCookie(name) { const item = document.cookie.split(';').find(value => value.trim().startsWith(`${name}=`)); return item ? decodeURIComponent(item.trim().split('=')[1]) : null }
function escapeHtml(value) { return String(value).replace(/[&<>'"]/g, character => ({'&':'&amp;','<':'&lt;','>':'&gt;',"'":'&#39;','"':'&quot;'}[character])) }

class Countdown {
  constructor(element, initialTime) { this.element = element; this.remaining = this.parseTime(initialTime); this.timer = setInterval(() => this.update(), 1000) }
  parseTime(value) { const [hours, minutes, seconds] = value.split(':').map(Number); return hours * 3600 + minutes * 60 + seconds }
  update() { this.remaining = Math.max(0, this.remaining - 1); this.element.textContent = `${String(Math.floor(this.remaining / 3600)).padStart(2, '0')}:${String(Math.floor(this.remaining / 60) % 60).padStart(2, '0')}:${String(this.remaining % 60).padStart(2, '0')}`; if (!this.remaining) clearInterval(this.timer) }
}
