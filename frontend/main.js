// Отображение модального окна при загрузке страницы если пользователь не авторизован
document.addEventListener('DOMContentLoaded', checkAuthentication);



// Функция проверки авторизации

function checkAuthentication() {
    const token = localStorage.getItem('authToken');

    if (token) {
        // Проверка валидности токена
        fetch('http://localhost:7070/api/check_user', {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        })
            .then(response => response.json())
            .then(data => {
                if (data.valid) {
                    // Пользователь авторизован
                    showAuthenticatedUI(data.full_name);
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



// Переключение между вкладками

const authModal = document.getElementById('auth-modal');
const authTabs = document.querySelectorAll('.auth-tab');
const authForms = document.querySelectorAll('.auth-form');

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



// Отображение UI

const mainContent = document.getElementById('wrapper');
const userInfo = document.getElementById('user-info');
const userName = document.getElementById('user-name');

// Отображение UI приложения после успешной аутентификации

function showAuthenticatedUI(username) {
    authModal.style.display = 'none';
    mainContent.style.display = 'block';
    userInfo.style.display = 'block';
    userName.textContent = username || 'Пользователь';
}

// Отображение модального окна аутентификации

function showAuthModal() {
    authModal.style.display = 'flex';
    mainContent.style.display = 'none';
    userInfo.style.display = 'none';
}



// Обработка формы входа

const loginForm = document.getElementById('login-form-element');

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
            'Content-Type': 'application/json'
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
                showAuthenticatedUI(data.full_name);
            } else {
                // Ошибка входа
                loginError.textContent = data.error || 'Неверный email или пароль';
            }
        })
        .catch(error => {
            console.error('Ошибка:', error);
            loginError.textContent = 'Ошибка при выполнении запроса';
        });
});



// Обработка формы регистрации

const registerForm = document.getElementById('register-form-element');

registerForm.addEventListener('submit', function (e) {
    e.preventDefault();
    const registerError = document.getElementById('register-error');
    registerError.textContent = '';

    const fullname = document.getElementById('register-name').value;
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
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            full_name: fullname,
            password: password,
            email: email
        })
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                // Успешный вход
                localStorage.setItem('authToken', data.token);
                showAuthenticatedUI(fullname);
            } else {
                // Ошибка регистрации
                registerError.textContent = data.error || 'Ошибка при регистрации';
            }
        })
        .catch(error => {
            console.error('Ошибка:', error);
            registerError.textContent = 'Ошибка при выполнении запроса';
        });
});



// Обработка выхода из аккаунта

const logoutButton = document.getElementById('logout-button');

logoutButton.addEventListener('click', () => {
    localStorage.removeItem('authToken');
    showAuthModal();
});