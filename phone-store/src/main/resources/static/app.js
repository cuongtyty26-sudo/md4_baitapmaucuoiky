/* ===== Tiện ích ===== */
const $ = id => document.getElementById(id);
const fmt = n => Number(n || 0).toLocaleString('vi-VN');
const esc = s => String(s ?? '').replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
const STATUS = { PENDING: 'Chờ xử lý', COMPLETED: 'Hoàn thành', CANCELLED: 'Đã huỷ' };

async function api(path, method = 'GET', body) {
  const res = await fetch('/api' + path, {
    method,
    headers: body ? { 'Content-Type': 'application/json' } : {},
    body: body ? JSON.stringify(body) : undefined
  });
  if (res.status === 401) { location.href = 'login.html'; throw new Error('Chưa đăng nhập'); }
  if (res.status === 204) return null;
  const data = await res.json().catch(() => ({}));
  if (!res.ok) throw new Error(data.message || 'Có lỗi xảy ra');
  return data;
}

function notify(text, type = 'success') {
  const box = $('alertBox');
  box.className = 'alert alert-' + type;
  box.textContent = text;
  setTimeout(() => box.classList.add('d-none'), 3500);
}

function renderPager(el, page, totalPages, onGo) {
  el.innerHTML = '';
  if (totalPages <= 1) return;
  for (let i = 0; i < totalPages; i++) {
    const li = document.createElement('li');
    li.className = 'page-item' + (i === page ? ' active' : '');
    li.innerHTML = `<button class="page-link">${i + 1}</button>`;
    li.onclick = () => onGo(i);
    el.appendChild(li);
  }
}

/* ===== Xác thực & tab ===== */
(async () => {
  try { $('who').textContent = (await api('/auth/me')).username; } catch (e) { return; }
  loadProducts(0);
})();

$('logoutBtn').onclick = async () => { await api('/auth/logout', 'POST'); location.href = 'login.html'; };

document.querySelectorAll('#tabs .nav-link').forEach(btn => btn.onclick = () => {
  document.querySelectorAll('#tabs .nav-link').forEach(b => b.classList.remove('active'));
  btn.classList.add('active');
  document.querySelectorAll('.tab-pane-x').forEach(p => p.classList.add('d-none'));
  const t = btn.dataset.tab;
  $(t).classList.remove('d-none');
  if (t === 'products') loadProducts(0);
  if (t === 'customers') loadCustomers(0);
  if (t === 'invoices') loadInvoices(0);
  if (t === 'stats') loadStats();
});

/* ===== SẢN PHẨM ===== */
const mProduct = new bootstrap.Modal($('mProduct'));
let productCache = [];

async function loadProducts(page = 0) {
  const [sortBy, dir] = $('fSort').value.split('|');
  const q = new URLSearchParams({ page, size: 8, sortBy, dir });
  if ($('fBrand').value.trim()) q.set('brand', $('fBrand').value.trim());
  if ($('fMin').value) q.set('minPrice', $('fMin').value);
  if ($('fMax').value) q.set('maxPrice', $('fMax').value);
  if ($('fStock').value) q.set('stock', $('fStock').value);
  try {
    const d = await api('/products?' + q);
    productCache = d.content;
    $('tbProducts').innerHTML = d.content.length ? d.content.map(p => `
      <tr><td>${p.id}</td><td>${esc(p.name)}</td><td>${esc(p.brand)}</td>
      <td class="money">${fmt(p.price)}</td><td class="money">${p.stock}</td><td>${esc(p.description)}</td>
      <td class="text-nowrap"><button class="btn btn-sm btn-outline-primary" onclick="editProduct(${p.id})">Sửa</button>
      <button class="btn btn-sm btn-outline-danger" onclick="deleteProduct(${p.id})">Xoá</button></td></tr>`).join('')
      : '<tr><td colspan="7" class="text-center text-muted py-4">Chưa có sản phẩm phù hợp. Bấm "Thêm sản phẩm" để tạo mới.</td></tr>';
    renderPager($('pgProducts'), d.number, d.totalPages, loadProducts);
  } catch (e) { notify(e.message, 'danger'); }
}

$('btnSearchP').onclick = () => loadProducts(0);
$('fSort').onchange = () => loadProducts(0);

function openProduct(p) {
  $('mProductTitle').textContent = p ? 'Cập nhật sản phẩm #' + p.id : 'Thêm sản phẩm';
  $('pId').value = p?.id ?? '';
  $('pName').value = p?.name ?? ''; $('pBrand').value = p?.brand ?? '';
  $('pPrice').value = p?.price ?? ''; $('pStock').value = p?.stock ?? ''; $('pDesc').value = p?.description ?? '';
  $('pErr').classList.add('d-none');
  mProduct.show();
}
$('btnAddP').onclick = () => openProduct(null);
window.editProduct = id => openProduct(productCache.find(p => p.id === id));

$('pSave').onclick = async () => {
  const id = $('pId').value;
  const body = {
    name: $('pName').value.trim(), brand: $('pBrand').value.trim(),
    price: $('pPrice').value === '' ? null : Number($('pPrice').value),
    stock: $('pStock').value === '' ? null : Number($('pStock').value),
    description: $('pDesc').value.trim()
  };
  try {
    await api(id ? '/products/' + id : '/products', id ? 'PUT' : 'POST', body);
    mProduct.hide(); notify(id ? 'Đã cập nhật sản phẩm' : 'Đã thêm sản phẩm');
    loadProducts(0);
  } catch (e) { $('pErr').textContent = e.message; $('pErr').classList.remove('d-none'); }
};

window.deleteProduct = async id => {
  if (!confirm('Bạn có chắc muốn xoá sản phẩm #' + id + '?')) return;
  try { await api('/products/' + id, 'DELETE'); notify('Đã xoá sản phẩm'); loadProducts(0); }
  catch (e) { notify(e.message, 'danger'); }
};

/* ===== KHÁCH HÀNG ===== */
const mCustomer = new bootstrap.Modal($('mCustomer'));
let customerCache = [];

async function loadCustomers(page = 0) {
  try {
    const d = await api('/customers?page=' + page + '&size=8');
    customerCache = d.content;
    $('tbCustomers').innerHTML = d.content.length ? d.content.map(c => `
      <tr><td>${c.id}</td><td>${esc(c.name)}</td><td>${esc(c.phone)}</td><td>${esc(c.email)}</td><td>${esc(c.address)}</td>
      <td class="text-nowrap"><button class="btn btn-sm btn-outline-primary" onclick="editCustomer(${c.id})">Sửa</button>
      <button class="btn btn-sm btn-outline-danger" onclick="deleteCustomer(${c.id})">Xoá</button></td></tr>`).join('')
      : '<tr><td colspan="6" class="text-center text-muted py-4">Chưa có khách hàng. Bấm "Thêm khách hàng" để tạo mới.</td></tr>';
    renderPager($('pgCustomers'), d.number, d.totalPages, loadCustomers);
  } catch (e) { notify(e.message, 'danger'); }
}

function openCustomer(c) {
  $('mCustomerTitle').textContent = c ? 'Cập nhật khách hàng #' + c.id : 'Thêm khách hàng';
  $('cId').value = c?.id ?? '';
  $('cName').value = c?.name ?? ''; $('cPhone').value = c?.phone ?? '';
  $('cEmail').value = c?.email ?? ''; $('cAddress').value = c?.address ?? '';
  $('cErr').classList.add('d-none');
  mCustomer.show();
}
$('btnAddC').onclick = () => openCustomer(null);
window.editCustomer = id => openCustomer(customerCache.find(c => c.id === id));

$('cSave').onclick = async () => {
  const id = $('cId').value;
  const body = { name: $('cName').value.trim(), phone: $('cPhone').value.trim(),
                 email: $('cEmail').value.trim() || null, address: $('cAddress').value.trim() };
  try {
    await api(id ? '/customers/' + id : '/customers', id ? 'PUT' : 'POST', body);
    mCustomer.hide(); notify(id ? 'Đã cập nhật khách hàng' : 'Đã thêm khách hàng');
    loadCustomers(0);
  } catch (e) { $('cErr').textContent = e.message; $('cErr').classList.remove('d-none'); }
};

window.deleteCustomer = async id => {
  if (!confirm('Bạn có chắc muốn xoá khách hàng #' + id + '?')) return;
  try { await api('/customers/' + id, 'DELETE'); notify('Đã xoá khách hàng'); loadCustomers(0); }
  catch (e) { notify(e.message, 'danger'); }
};

/* ===== HÓA ĐƠN ===== */
const mInvoice = new bootstrap.Modal($('mInvoice'));
let allProducts = [];

async function loadInvoices(page = 0) {
  const q = new URLSearchParams({ page, size: 6 });
  if ($('fInv').value.trim()) q.set('keyword', $('fInv').value.trim());
  try {
    const d = await api('/invoices?' + q);
    $('tbInvoices').innerHTML = d.content.length ? d.content.map(i => `
      <tr><td>HD${String(i.id).padStart(5, '0')}</td>
      <td>${esc(i.customer.name)}<div class="small text-muted">${esc(i.customer.phone)}</div></td>
      <td>${new Date(i.createdAt).toLocaleString('vi-VN')}</td>
      <td><ul class="invoice-lines">${i.items.map(it => `<li>${esc(it.product.name)} × ${it.quantity} (${fmt(it.unitPrice)})</li>`).join('')}</ul></td>
      <td class="money">${fmt(i.total)}</td>
      <td>${i.status === 'CANCELLED' ? '<span class="badge text-bg-secondary">Đã huỷ</span>' : `
        <select class="form-select form-select-sm" onchange="changeStatus(${i.id}, this.value)">
          ${['PENDING', 'COMPLETED', 'CANCELLED'].map(s => `<option value="${s}" ${s === i.status ? 'selected' : ''}>${STATUS[s]}</option>`).join('')}
        </select>`}</td></tr>`).join('')
      : '<tr><td colspan="6" class="text-center text-muted py-4">Không có hóa đơn phù hợp.</td></tr>';
    renderPager($('pgInvoices'), d.number, d.totalPages, loadInvoices);
  } catch (e) { notify(e.message, 'danger'); }
}
$('btnSearchI').onclick = () => loadInvoices(0);
$('fInv').addEventListener('keydown', e => { if (e.key === 'Enter') loadInvoices(0); });

window.changeStatus = async (id, status) => {
  if (status === 'CANCELLED' && !confirm('Huỷ đơn sẽ hoàn lại tồn kho và không thể đổi lại. Tiếp tục?')) return loadInvoices(0);
  try { await api('/invoices/' + id + '/status', 'PATCH', { status }); notify('Đã cập nhật trạng thái'); }
  catch (e) { notify(e.message, 'danger'); }
  loadInvoices(0);
};

function lineHtml() {
  const opts = allProducts.map(p => `<option value="${p.id}" data-price="${p.price}">${esc(p.name)} - ${fmt(p.price)}đ (còn ${p.stock})</option>`).join('');
  return `<div class="row g-2 mb-2 line">
    <div class="col-8"><select class="form-select l-prod">${opts}</select></div>
    <div class="col-3"><input type="number" min="1" value="1" class="form-control l-qty"></div>
    <div class="col-1"><button class="btn btn-outline-danger w-100 l-del" title="Xoá dòng">×</button></div></div>`;
}

function calcTotal() {
  let t = 0;
  document.querySelectorAll('#iLines .line').forEach(l => {
    const price = Number(l.querySelector('.l-prod').selectedOptions[0]?.dataset.price || 0);
    t += price * Number(l.querySelector('.l-qty').value || 0);
  });
  $('iTotal').textContent = fmt(t);
}

function addLine() {
  $('iLines').insertAdjacentHTML('beforeend', lineHtml());
  const l = $('iLines').lastElementChild;
  l.querySelector('.l-prod').onchange = calcTotal;
  l.querySelector('.l-qty').oninput = calcTotal;
  l.querySelector('.l-del').onclick = () => { l.remove(); calcTotal(); };
  calcTotal();
}

$('btnAddI').onclick = async () => {
  try {
    const [cs, ps] = await Promise.all([api('/customers?size=1000'), api('/products?size=1000')]);
    allProducts = ps.content.filter(p => p.stock > 0);
    if (!cs.content.length || !allProducts.length)
      return notify('Cần có ít nhất 1 khách hàng và 1 sản phẩm còn hàng để tạo đơn', 'warning');
    $('iCustomer').innerHTML = cs.content.map(c => `<option value="${c.id}">${esc(c.name)} - ${esc(c.phone)}</option>`).join('');
    $('iLines').innerHTML = ''; $('iErr').classList.add('d-none');
    addLine(); mInvoice.show();
  } catch (e) { notify(e.message, 'danger'); }
};
$('iAddLine').onclick = addLine;

$('iSave').onclick = async () => {
  const items = [...document.querySelectorAll('#iLines .line')].map(l => ({
    productId: Number(l.querySelector('.l-prod').value), quantity: Number(l.querySelector('.l-qty').value)
  }));
  try {
    await api('/invoices', 'POST', { customerId: Number($('iCustomer').value), items });
    mInvoice.hide(); notify('Đã tạo đơn hàng và trừ tồn kho'); loadInvoices(0);
  } catch (e) { $('iErr').textContent = e.message; $('iErr').classList.remove('d-none'); }
};

/* ===== DOANH THU ===== */
async function loadStats() {
  const fill = async (path, el) => {
    const rows = await api('/statistics/' + path);
    $(el).innerHTML = rows.length ? rows.map(r =>
      `<tr><td>${r.period}</td><td class="money">${r.orders}</td><td class="money">${fmt(r.revenue)}</td></tr>`).join('')
      : '<tr><td colspan="3" class="text-muted">Chưa có dữ liệu</td></tr>';
  };
  try { await Promise.all([fill('daily', 'stDay'), fill('monthly', 'stMonth'), fill('yearly', 'stYear')]); }
  catch (e) { notify(e.message, 'danger'); }
}
