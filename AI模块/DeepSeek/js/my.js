const userAvatarUrl = "./img/yh.png"; // Example user avatar URL
const botAvatarUrl = "./img/i1.jpg"; // Example bot avatar URL

let fileData = null;
let isPaused = false; // 标志变量，用于控制文字生成的暂停和恢复

function handleFileUpload(event) {
    const file = event.target.files[0];
    if (file) {
        const reader = new FileReader();
        reader.onload = function(e) {
            fileData = e.target.result.split(',')[1]; // Get Base64 part
            document.getElementById('waitingMessage').textContent = `Uploaded: ${file.name}`;
            document.getElementById('waitingMessage').classList.add('visible');
        };
        reader.readAsDataURL(file);
    }
}

async function sendMessage() {
    const userInput = document.getElementById('userInput').value;
    if (!userInput && !fileData) return;

    // Hide welcome message after first message is sent
    document.getElementById('welcomeMessage').style.display = 'none';

    // Hide waiting message
    document.getElementById('waitingMessage').classList.remove('visible');

    // Display user message with HTML entity encoding
    let messageContent = escapeHtml(userInput);
    if (fileData) {
        messageContent += `\nAttached file: ${document.getElementById('fileUpload').files[0].name}`;
    }
    appendMessage(messageContent, 'user');
    document.getElementById('userInput').value = '';
    const fileName = fileData ? document.getElementById('fileUpload').files[0].name : null;
    fileData = null;

    // Show thinking message
    const thinkingMessageElement = appendMessage('正在思考...', 'bot');

    // Call DeepSeek API
    try {
        const response = await fetch('https://api.deepseek.com/v1/chat/completions', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer sk-815a7e87ba7c4ebc9ee771adfa59e3ad'
            },
            body: JSON.stringify({
                model: "deepseek-chat",
                messages: [
                  { role: "user", content: userInput }
                ],
                file: fileData ? { name: fileName, content: fileData } : null,
                temperature: 0.5
              })
        });

        if (!response.ok) {
            console.error("Response status:", response.status);
            console.error("Response body:", await response.text());
            throw new Error(`HTTP error! Status: ${response.status}`);
        }

        const data = await response.json();
        const botResponse = data.choices[0].message.content || "I'm sorry, I couldn't understand that.";

        // Remove thinking message
        thinkingMessageElement.remove();

        // 显示机器人消息并进行HTML实体编码
        typeWriter(botResponse, 'bot');
    } catch (error) {
        console.error("Error fetching the response:", error);

        // Remove thinking message
        thinkingMessageElement.remove();

        appendMessage("Oops! Something went wrong.", 'bot');
    }
}

function showHistory() {
    // 这里可以添加显示历史会话记录的逻辑
    console.log("显示历史会话记录");
}

function startNewChat() {
    document.getElementById('messages').innerHTML = ''; // 清空消息列表
    document.getElementById('welcomeMessage').style.display = 'block'; // 显示欢迎信息
}

let messagePauseStates = {}; // 用于存储每个消息元素的暂停状态

function appendMessage(message, sender) {
    const messagesContainer = document.getElementById('messages');
    const messageElement = document.createElement('div');
    const messageId = `message-${Date.now()}-${Math.floor(Math.random() * 1000)}`; // 生成唯一标识符
    messageElement.id = messageId;
    messageElement.classList.add('message', sender);

    const avatarElement = document.createElement('img');
    avatarElement.src = sender === 'user' ? userAvatarUrl : botAvatarUrl;
    avatarElement.alt = `${sender} avatar`;
    avatarElement.classList.add('avatar', sender);

    const textElement = document.createElement('div');
    textElement.classList.add('text', sender);
    
    // 如果是机器人的消息，转换为Markdown格式
    if (sender === 'bot') {
        textElement.innerHTML = markdownToHtml(message);
    } else {
        textElement.innerHTML = escapeHtml(message); // Use innerHTML for encoded content
    }

    if (sender === 'user') {
        messageElement.appendChild(textElement);
        messageElement.appendChild(avatarElement);
    } else if (sender === 'bot') {
        messageElement.appendChild(avatarElement);
        messageElement.appendChild(textElement);

        // 创建按钮栏
        const buttonBar = document.createElement('div');
        buttonBar.classList.add('button-bar');

        // Add pause button for bot messages
        const pauseButton = document.createElement('button');
        pauseButton.classList.add('pause-button');
        pauseButton.innerHTML = '<i class="fas fa-pause"></i>'; // 使用 Font Awesome 的暂停图标
        pauseButton.onclick = function() {
            messagePauseStates[messageId] = !messagePauseStates[messageId];
            if (messagePauseStates[messageId]) {
                pauseButton.innerHTML = '<i class="fas fa-play"></i>'; // 使用 Font Awesome 的播放图标
            } else {
                pauseButton.innerHTML = '<i class="fas fa-pause"></i>'; // 使用 Font Awesome 的暂停图标
            }
        };
        buttonBar.appendChild(pauseButton);

        // Add copy button for bot messages
        const copyButton = document.createElement('button');
        copyButton.classList.add('copy-button');
        copyButton.innerHTML = '<i class="fas fa-copy"></i>'; // 使用 Font Awesome 的复制图标
        copyButton.onclick = function() {
            const textToCopy = textElement.innerText; // 获取消息框中的文本内容
            navigator.clipboard.writeText(textToCopy)
                .then(() => {
                    console.log('Text copied to clipboard');

                    // 显示已复制提示
                    const copySuccessMessage = document.createElement('div');
                    copySuccessMessage.classList.add('copy-success');
                    copySuccessMessage.textContent = '已复制到剪切板';
                    messageElement.appendChild(copySuccessMessage);

                    // 隐藏提示
                    setTimeout(() => {
                        copySuccessMessage.remove();
                    }, 2000); // 2秒后隐藏提示
                })
                .catch(err => {
                    console.error('Failed to copy text: ', err);
                });
        };
        buttonBar.appendChild(copyButton);

        // 将按钮栏添加到消息元素中，并确保它位于左下角
        messageElement.appendChild(buttonBar);
    }

    messagesContainer.appendChild(messageElement);

    // Scroll to bottom of messages container
    messagesContainer.scrollTop = messagesContainer.scrollHeight;

    // 初始化暂停状态
    messagePauseStates[messageId] = false;

    // Return the message element for removal if needed
    return messageElement;
}
function escapeHtml(unsafe) {
    return unsafe
         .replace(/&/g, "&amp;")
         .replace(/</g, "&lt;")
         .replace(/>/g, "&gt;")
         .replace(/"/g, "&quot;")
         .replace(/'/g, "&#039;");
}

function markdownToHtml(markdown) {
    // 简单的Markdown解析器
    // 处理粗体和斜体
    let html = markdown.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');
    html = html.replace(/\*(.*?)\*/g, '<em>$1</em>');
    // 处理列表
    html = html.replace(/^- (.*?)$/gm, '<li>$1</li>');
    html = html.replace(/<li>(.*?)<\/li>/g, '<ul>$&</ul>');
    // 处理链接
    html = html.replace(/\[(.*?)\]\((.*?)\)/g, '<a href="$2">$1</a>');
    // 处理代码块
    html = html.replace(/```(.*?)\n([\s\S]*?)\n```/g, '<pre><code class="$1">$2</code></pre>');
    // 处理行内代码
    html = html.replace(/`(.*?)`/g, '<code>$1</code>');
    return html;
}

function typeWriter(text, sender) {
    let i = 0;
    const messageElement = appendMessage('', sender);
    const textElement = messageElement.querySelector('.text');
    const messageId = messageElement.id; // 获取消息元素的唯一标识符

    function writeCharacter() {
        if (i < text.length) {
            if (!messagePauseStates[messageId]) { // 检查当前消息元素的暂停状态
                textElement.innerHTML += text.charAt(i);
                i++;
            }
            setTimeout(writeCharacter, 50); // 50ms delay between each character
        }
    }

    writeCharacter();
}