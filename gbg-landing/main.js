// Обработка модального окна аутентификации
const authModal = document.getElementById('auth-modal');
const authTabs = document.querySelectorAll('.auth-tab');
const authForms = document.querySelectorAll('.auth-form');

const loginForm = document.getElementById('login-form-element');
const registerForm = document.getElementById('register-form-element');

const mainContent = document.getElementById('wrapper');

const userInfo = document.getElementById('user-info');
const userName = document.getElementById('user-name');
const logoutButton = document.getElementById('logout-button');

// Отображение модального окна при загрузке страницы если пользователь не авторизован
document.addEventListener('DOMContentLoaded', checkAuthentication);

// Переключение между вкладками
authTabs.forEach(tab => {
    tab.addEventListener('click', () => {
        const tabName = tab.getAttribute('data-tab');

        // Активация выбранной вкладки
        authTabs.forEach(t => t.classList.remove('active'));
        tab.classList.add('active');

        // Отображение соответствующей формы
        authForms.forEach(form => {
            form.classList.remove('active');
            if (form.id === `${tabName}-form`) {
                form.classList.add('active');
            }
        });

        // Сброс сообщений об ошибках
        document.getElementById('login-error').textContent = '';
        document.getElementById('register-error').textContent = '';
    });
});

// Функция проверки авторизации
function checkAuthentication() {
    const token = localStorage.getItem('authToken');

    if (token) {
        // Проверка валидности токена
        fetch('http://localhost:7070/api/verify-token', {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        })
            .then(response => response.json())
            .then(data => {
                if (data.valid) {
                    // Пользователь авторизован
                    showAuthenticatedUI(data.user);
                } else {
                    // Токен недействителен
                    localStorage.removeItem('authToken');
                    showAuthModal();
                }
            })
            .catch(error => {
                console.error('Ошибка:', error);
                localStorage.removeItem('authToken');
                showAuthModal();
            });
    } else {
        // Токен отсутствует, показать модальное окно
        showAuthModal();
    }
}

// Отображение UI для авторизованного пользователя
function showAuthenticatedUI(user) {
    authModal.style.display = 'none';
    mainContent.style.display = 'block';
    userInfo.style.display = 'block';
    userName.textContent = user?.name || 'Пользователь';
}

// Отображение модального окна аутентификации
function showAuthModal() {
    authModal.style.display = 'flex';
    mainContent.style.display = 'none';
    userInfo.style.display = 'none';
}

// Обработка формы входа
loginForm.addEventListener('submit', function (e) {
    e.preventDefault();
    const loginError = document.getElementById('login-error');
    loginError.textContent = '';

    const email = document.getElementById('login-email').value;
    const password = document.getElementById('login-password').value;

    // API запрос для входа
    fetch('http://localhost:7070/api/login', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            email: email,
            password: password
        })
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                // Успешный вход
                localStorage.setItem('authToken', data.token);
                showAuthenticatedUI(data.user);
            } else {
                // Ошибка входа
                loginError.textContent = data.message || 'Неверный email или пароль';
            }
        })
        .catch(error => {
            console.error('Ошибка:', error);
            loginError.textContent = 'Ошибка при выполнении запроса';
        });
});

// Обработка формы регистрации
registerForm.addEventListener('submit', function (e) {
    e.preventDefault();
    const registerError = document.getElementById('register-error');
    registerError.textContent = '';

    const name = document.getElementById('register-name').value;
    const email = document.getElementById('register-email').value;
    const password = document.getElementById('register-password').value;
    const passwordConfirm = document.getElementById('register-password-confirm').value;

    // Проверка совпадения паролей
    if (password !== passwordConfirm) {
        registerError.textContent = 'Пароли не совпадают';
        return;
    }

    // API запрос для регистрации
    fetch('http://localhost:7070/api/register', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            name: name,
            email: email,
            password: password
        })
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                // Успешная регистрация и автоматический вход
                localStorage.setItem('authToken', data.token);
                showAuthenticatedUI(data.user);
            } else {
                // Ошибка регистрации
                registerError.textContent = data.message || 'Ошибка при регистрации';
            }
        })
        .catch(error => {
            console.error('Ошибка:', error);
            registerError.textContent = 'Ошибка при выполнении запроса';
        });
});

// Обработка выхода из аккаунта
logoutButton.addEventListener('click', () => {
    // API запрос для выхода
    fetch('http://localhost:7070/api/logout', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${localStorage.getItem('authToken')}`
        }
    })
        .then(response => response.json())
        .then(data => {
            // Удаление токена из localStorage
            localStorage.removeItem('authToken');
            showAuthModal();
        })
        .catch(error => {
            console.error('Ошибка:', error);
            localStorage.removeItem('authToken');
            showAuthModal();
        });
});