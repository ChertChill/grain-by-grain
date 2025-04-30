// Отображение модального окна при загрузке страницы если пользователь не авторизован
document.addEventListener('DOMContentLoaded', checkAuthentication);



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



// Аутентификация пользователя

const loginForm = document.getElementById('login-form-element');

loginForm.addEventListener('submit', function (e) {
    e.preventDefault(); // Отмена стандартного поведения формы
    loginUser();
});



// Регистрация пользователя

const registerForm = document.getElementById('register-form-element');

registerForm.addEventListener('submit', function (e) {
    e.preventDefault(); // Отмена стандартного поведения формы
    registerUser();
});



// Получение транзакций по кнопке "Сброс параметров"

const resetButton = document.getElementById('reset-button');

resetButton.addEventListener('click', () => {
    getUserTransactions();
});



// Выход из аккаунта

const logoutButton = document.getElementById('logout-button');

logoutButton.addEventListener('click', () => {
    localStorage.removeItem('authToken');
    showAuthModal();
});






/* ----------------------- */
/* -- Отдельные функции -- */
/* ----------------------- */



// Отображение UI

const mainContent = document.getElementById('wrapper');
const userInfo = document.getElementById('user-info');
const userName = document.getElementById('user-name');

// Функция для отображение UI приложения после успешной аутентификации

function showAuthenticatedUI(username) {
    authModal.style.display = 'none';
    mainContent.style.display = 'block';
    userInfo.style.display = 'block';
    userName.textContent = formatFullName(username) || 'Пользователь';
}

// Функция для отображение модального окна аутентификации

function showAuthModal() {
    authModal.style.display = 'flex';
    mainContent.style.display = 'none';
    userInfo.style.display = 'none';
}



// Функция для форматирования ФИО

function formatFullName(fullName) {
    if (!fullName) return '';
    
    const parts = fullName.split(' ');
    if (parts.length === 0) return '';
    
    // Фамилия всегда первая
    const lastName = parts[0];
    
    // Если есть имя и отчество
    if (parts.length >= 2) {
        const firstName = parts[1].charAt(0) + '.';
        if (parts.length >= 3) {
            const middleName = parts[2].charAt(0) + '.';
            return `${lastName} ${firstName} ${middleName}`;
        }
        return `${lastName} ${firstName}`;
    }
    
    return lastName;
}



// Функция для регистрации пользователя

function registerUser() {
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

                // Очистка списка транзакций
                const transactionsList = document.getElementById('transactions-list');
                if (transactionsList) {
                    transactionsList.innerHTML = '';
                }

                // Очистка сообщения об ошибке
                const transactionsError = document.getElementById('transactions-error');
                if (transactionsError) {
                    transactionsError.textContent = '';
                }

                // Переключение на вкладку аутентификации
                const loginTab = document.querySelector('.auth-tab[data-tab="login"]');
                loginTab.click();
            } else {
                // Ошибка регистрации
                registerError.textContent = data.error || 'Ошибка при регистрации';
            }
        })
        .catch(error => {
            console.error('Ошибка:', error);
            registerError.textContent = 'Ошибка при выполнении запроса';
        });
};



// Функция для авторизации пользователя

function loginUser() {
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
                showAuthenticatedUI(formatFullName(data.full_name));
                getUserTransactions();
            } else {
                // Ошибка входа
                loginError.textContent = data.error || 'Неверный email или пароль';
            }
        })
        .catch(error => {
            console.error('Ошибка:', error);
            loginError.textContent = 'Ошибка при выполнении запроса';
        });
};



// Функция для проверки авторизации

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
                    showAuthenticatedUI(formatFullName(data.full_name));
                    getUserTransactions();
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



// Функция для получения транзакций пользователя

function getUserTransactions() {
    const transactionsError = document.getElementById('transactions-error');
    if (transactionsError) {
        transactionsError.textContent = '';
    }

    const token = localStorage.getItem('authToken');

    fetch('http://localhost:7070/api/get_transactions', {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
        .then(response => response.json())
        .then(data => {
            const transactionsList = document.getElementById('transactions-list');
            transactionsList.innerHTML = ''; // Clear existing transactions

            if (data.transactions && Array.isArray(data.transactions)) {
                data.transactions.forEach(transaction => {
                    const transactionEl = document.createElement('div');
                    transactionEl.className = 'transaction-item';

                    // Format date
                    const date = new Date(Date.UTC(...transaction.transactionDate));
                    const formattedDate = date.toLocaleDateString('ru-RU', {
                        year: 'numeric',
                        month: 'long',
                        day: 'numeric'
                    });

                    // Create transaction HTML
                    transactionEl.innerHTML = `
                        <div class="ti-content">
                            <div class="ti-main-info">
                                <div class="ti-type ${transaction.type.typeID === 1 ? 'income' : 'expense'}">
                                    ${transaction.type.name}
                                </div>
                                <div class="ti-status ${getStatusClass(transaction.status.name)}">
                                    ${transaction.status.name}
                                </div>
                            </div>
                            
                            <div class="ti-secondary-info">
                                <div class="ti-amount-block">
                                    <div class="ti-info-group">
                                        <div class="ti-info-label">Сумма</div>
                                        <div class="ti-info-value ti-amount">
                                            ${transaction.amount.toLocaleString('ru-RU')} р
                                        </div>
                                    </div>
                                </div>

                                <div class="ti-details-block">
                                    <div class="ti-info-group">
                                        <div class="ti-info-label">Категория</div>
                                        <div class="ti-info-value ti-category">${transaction.category.name}</div>
                                    </div>
                                    <div class="ti-info-group">
                                        <div class="ti-info-label">Описание</div>
                                        <div class="ti-info-value ti-comment">${transaction.comment}</div>
                                    </div>
                                </div>

                                <div class="ti-amount-block">
                                    <div class="ti-info-group">
                                        <div class="ti-info-label">Дата</div>
                                        <div class="ti-info-value">
                                            ${formattedDate}
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div class="ti-details">
                                <div class="ti-details-toggle">Показать детали</div>
                                <div class="ti-details-content">
                                    <div class="ti-detail-item">
                                        <div class="ti-detail-label">Банк</div>
                                        <div class="ti-detail-value">${transaction.bank.name}</div>
                                    </div>
                                    <div class="ti-detail-item">
                                        <div class="ti-detail-label">Счет списания</div>
                                        <div class="ti-detail-value">${transaction.accountNumber}</div>
                                    </div>
                                    <div class="ti-detail-item">
                                        <div class="ti-detail-label">Счет получателя</div>
                                        <div class="ti-detail-value">${transaction.recipientNumber}</div>
                                    </div>
                                    <div class="ti-detail-item">
                                        <div class="ti-detail-label">ИНН получателя</div>
                                        <div class="ti-detail-value">${transaction.recipientTIN}</div>
                                    </div>
                                    <div class="ti-detail-item">
                                        <div class="ti-detail-label">Телефон получателя</div>
                                        <div class="ti-detail-value">${transaction.recipientPhone}</div>
                                    </div>
                                    <div class="ti-detail-item">
                                        <div class="ti-detail-label">Тип лица</div>
                                        <div class="ti-detail-value">${transaction.legalType.name}</div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="ti-buttons">
                            <div id="ti-edit" class="ti__button"></div>
                            <div id="ti-apply" class="ti__button"></div>
                            <div id="ti-delete" class="ti__button"></div>
                        </div>
                    `;

                    // Add click handler for details toggle
                    const detailsToggle = transactionEl.querySelector('.ti-details-toggle');
                    const detailsContent = transactionEl.querySelector('.ti-details-content');
                    
                    detailsToggle.addEventListener('click', () => {
                        detailsContent.classList.toggle('active');
                        detailsToggle.classList.toggle('active');
                        detailsToggle.textContent = detailsToggle.classList.contains('active') 
                            ? 'Скрыть детали' 
                            : 'Показать детали';
                    });

                    transactionsList.appendChild(transactionEl);
                });
            }
        })
        .catch(error => {
            console.error('Ошибка:', error);
            transactionsError.textContent = error.message || 'Ошибка при загрузке транзакций';
        });
}

// Helper function to get status class
function getStatusClass(status) {
    const statusMap = {
        'Новая': 'new',
        'Подтвержденная': 'confirmed',
        'В обработке': 'processing',
        'Отменена': 'cancelled',
        'Платеж выполнен': 'completed',
        'Платеж удален': 'deleted',
        'Возврат': 'refund'
    };
    return statusMap[status] || 'new';
}