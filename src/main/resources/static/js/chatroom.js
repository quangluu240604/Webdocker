(() => {
  const messagesContainer = document.getElementById('chat-room-messages');
  const form = document.getElementById('chat-room-form');
  const input = document.getElementById('chat-room-input');
  const senderSelect = document.getElementById('chat-room-sender');
  const wrapper = document.querySelector('.chat-room__wrapper');
  const sanitize = (text) => text.replace(/</g, '&lt;').replace(/>/g, '&gt;');
  if (!messagesContainer || !form || !input || !senderSelect || !wrapper) {
    return;
  }

  const isAdminSession = wrapper.dataset.isAdmin === 'true';
  const currentUser = wrapper.dataset.currentUser || 'Bạn';
  if (!isAdminSession) {
    senderSelect.value = 'USER';
    senderSelect.setAttribute('disabled', 'disabled');
  }

  const csrfTokenMeta = document.querySelector('meta[name="_csrf"]');
  const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');
  const csrfToken = csrfTokenMeta ? csrfTokenMeta.getAttribute('content') : '';
  const csrfHeader = csrfHeaderMeta ? csrfHeaderMeta.getAttribute('content') : '';

  const buildMessage = (sender, role, body, time) => {
    const item = document.createElement('div');
    item.className = 'chat-room__message ' + (role === 'ADMIN' ? 'chat-room__message--admin' : 'chat-room__message--user');
    const senderEl = document.createElement('div');
    senderEl.className = 'chat-room__message-sender';
    senderEl.textContent = sender;
    const bodyEl = document.createElement('p');
    bodyEl.textContent = sanitize(body);
    const timeEl = document.createElement('span');
    timeEl.className = 'chat-room__message-time';
    timeEl.textContent = time;
    item.appendChild(senderEl);
    item.appendChild(bodyEl);
    item.appendChild(timeEl);
    messagesContainer.appendChild(item);
    messagesContainer.scrollTop = messagesContainer.scrollHeight;
  };

  form.addEventListener('submit', async (event) => {
    event.preventDefault();
    const message = input.value.trim();
    if (!message) {
      return;
    }
    const role = senderSelect.value === 'ADMIN' ? 'ADMIN' : 'USER';
    const payload = {
      body: message,
      role
    };
    const headers = {
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    };
    if (csrfHeader && csrfToken) {
      headers[csrfHeader] = csrfToken;
    }
    try {
      const response = await fetch('/chat-room/messages', {
        method: 'POST',
        headers,
        body: JSON.stringify(payload)
      });
      console.log('chat send status', response.status);
      if (!response.ok) {
        const text = await response.text();
        throw new Error(`Không thể gửi tin nhắn (${response.status}) ${text}`);
      }
      const data = await response.json();
      buildMessage(data.sender, data.role, data.body, data.time);
      input.value = '';
      input.focus();
    } catch (error) {
      console.error(error);
    }
  });
})();
