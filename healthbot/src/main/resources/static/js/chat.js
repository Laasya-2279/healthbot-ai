let currentSessionId = null;
function loadMessages() {
    if (!currentSessionId) return;
    fetch(`/api/sessions/${currentSessionId}/messages`)
        .then(res => res.json())
        .then(data => {
            const messagesDiv = document.getElementById('messages');
            messagesDiv.innerHTML = '<div class="bot-msg"><strong>HealthBot:</strong> Hey there, describe your symptoms and I’ll help you like your personalized doctor 💙</div>';
            data.forEach(msg => {
                messagesDiv.innerHTML += `<div class="user-msg"><strong>You:</strong> ${msg.userMessage}</div>`;
                messagesDiv.innerHTML += `<div class="bot-msg"><strong>HealthBot:</strong> ${msg.botResponse}</div>`;
            });
            messagesDiv.scrollTop = messagesDiv.scrollHeight;
        });
}

function loadSessions() {
    fetch('/api/sessions')
        .then(res => res.json())
        .then(data => {
            const list = document.getElementById('sessionList');
            list.innerHTML = '';
            data.forEach(session => {
                const li = document.createElement('li');
                li.className = 'session-item d-flex justify-content-between align-items-center' +
                    (session.id === currentSessionId ? ' bg-primary text-white' : '');

                const span = document.createElement('span');
                span.innerText = session.title || `Chat ${session.id}`;
                span.onclick = () => {
                    currentSessionId = session.id;
                    loadMessages();
                };
                const delBtn = document.createElement('button');
                delBtn.innerHTML = '&times;';
                delBtn.className = 'btn btn-sm btn-outline-danger';
                delBtn.onclick = (e) => {
                    e.stopPropagation();
                    deleteSession(session.id);
                };

                li.appendChild(span);
                li.appendChild(delBtn);
                list.appendChild(li);
            });
        });
}

function startNewChat() {
    fetch('/api/sessions', {
        method: 'POST'
    })
        .then(res => res.json())
        .then(data => {
            currentSessionId = data.id;
            loadSessions();
            setTimeout(() => loadMessages(), 100);
        });
}
function sendMessage() {
    const input = document.getElementById('userInput');
    const message = input.value;
    if (!message || !currentSessionId) return;
    input.disabled = true;
    document.querySelector('button.btn-primary').disabled = true;
    const typingIndicator = document.getElementById('typingIndicator');
    if (typingIndicator) typingIndicator.style.display = 'flex';
    function formatGeminiResponse(text) {
        // Convert **bold** markdown
        text = text.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');

        // Convert * bullet points
        text = text.replace(/\n\* (.*?)\n/g, '<ul><li>$1</li></ul>');
        text = text.replace(/\* (.*?)(?=\n|$)/g, '<li>$1</li>');

        // Convert newlines to breaks
        text = text.replace(/\n/g, '<br>');

        return text;
    }

    if (!currentSessionId) {
        alert("Please start or select a chat session before sending a message.");
        return;
    }
    fetch(`/api/chat/${currentSessionId}`, {
        method: 'POST',
        headers: {'Content-Type': 'text/plain'},
        body: message
    })
        .then(res => res.text())
        .then(reply => {
            input.value = '';
            const formattedReply = formatGeminiResponse(reply);
            const messagesDiv = document.getElementById('messages');
            messagesDiv.innerHTML += `<div class="user-msg"><strong>You:</strong> ${message}</div>`;
            messagesDiv.innerHTML += `<div class="bot-msg"><strong>HealthBot:</strong> ${formattedReply}</div>`;
            messagesDiv.scrollTop = messagesDiv.scrollHeight;

        })
        .finally(() => {
            input.disabled = false;
            document.querySelector('button.btn-primary').disabled = false;
            if (typingIndicator) typingIndicator.style.display = 'none';
            input.focus();
        });
}

function deleteSession(id) {
    fetch(`/api/sessions/${id}`, {
        method: 'DELETE'
    }).then(() => loadSessions());
}

window.onload = () => {
    loadSessions();
    setTimeout(() => {
        if (!currentSessionId) {
            startNewChat();
        }
    }, 100);
}

