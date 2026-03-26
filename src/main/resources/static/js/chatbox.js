(() => {
  const widget = document.getElementById('chat-widget');
  if (!widget) {
    return;
  }

  const panel = document.getElementById('chat-panel');
  const toggle = document.getElementById('chat-toggle');
  const closeBtn = document.getElementById('chat-close');
  const form = document.getElementById('chat-form');
  const input = document.getElementById('chat-input');
  const messages = document.getElementById('chat-messages');
  const storageKey = 'greenshelfChatHistory';
  const storage = (() => {
    try {
      return window.sessionStorage;
    } catch (error) {
      return null;
    }
  })();

  const replies = [
    'Chào bạn! Tôi có thể giúp gì về sách hoặc đơn hàng nào?',
    'Bạn muốn tìm thể loại nào hôm nay?',
    'Đơn hàng của bạn đang chờ xử lý. Mình có thể kiểm tra trạng thái giúp.',
    'Cần tư vấn thêm về giao hàng xanh? Mình luôn sẵn sàng.',
    'Mình có thể kết nối bạn với chuyên gia của GreenShelf nếu cần.'
  ];

  let history = [];

  const saveHistory = () => {
    if (!storage) {
      return;
    }
    try {
      storage.setItem(storageKey, JSON.stringify(history));
    } catch (error) {
      console.debug('Không lưu được lịch sử chat:', error);
    }
  };

  const loadHistory = () => {
    try {
      const stored = storage ? storage.getItem(storageKey) : null;
      if (stored) {
        history = JSON.parse(stored);
      }
    } catch (error) {
      history = [];
    }
  };

  const appendMessage = (entry) => {
    const element = document.createElement('div');
    element.className = `chat-message${entry.from === 'user' ? ' chat-message--user' : ''}`;
    element.textContent = entry.text;
    messages.appendChild(element);
  };

  const scrollToBottom = () => {
    window.requestAnimationFrame(() => {
      messages.scrollTop = messages.scrollHeight;
    });
  };

  const pushMessage = (text, from) => {
    const entry = { text, from };
    history.push(entry);
    if (history.length > 60) {
      history = history.slice(-60);
    }
    saveHistory();
    appendMessage(entry);
    scrollToBottom();
  };

  const renderHistory = () => {
    messages.innerHTML = '';
    history.forEach(appendMessage);
    scrollToBottom();
  };

  const sendBotReply = () => {
    const reply = replies[Math.floor(Math.random() * replies.length)];
    window.setTimeout(() => {
      pushMessage(reply, 'bot');
    }, 600);
  };

  const openChat = () => {
    widget.classList.add('chat-widget--open');
    toggle.setAttribute('aria-expanded', 'true');
  };

  const closeChat = () => {
    widget.classList.remove('chat-widget--open');
    toggle.setAttribute('aria-expanded', 'false');
  };

  toggle.addEventListener('click', () => {
    if (widget.classList.contains('chat-widget--open')) {
      closeChat();
    } else {
      openChat();
    }
  });

  closeBtn.addEventListener('click', closeChat);

  form.addEventListener('submit', (event) => {
    event.preventDefault();
    const text = input.value.trim();
    if (!text) {
      return;
    }
    pushMessage(text, 'user');
    input.value = '';
    sendBotReply();
  });

  input.addEventListener('keydown', (event) => {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      form.requestSubmit();
    }
  });

  loadHistory();
  if (!history.length) {
    pushMessage('Chào mừng bạn đến với GreenShelf! Viết gì đó để bắt đầu trò chuyện nhé.', 'bot');
  } else {
    renderHistory();
  }
})();
