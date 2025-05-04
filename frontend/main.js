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
    
    // Ensure "Сводка по счету" tab is active
    document.getElementById('button-1').checked = true;
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
                    transactionEl.setAttribute('data-transaction-id', transaction.transactionID);

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
                                            <div class="ti-detail-label">Банк отправителя</div>
                                            <div class="ti-detail-value">${transaction.senderBank.name}</div>
                                        </div>
                                        <div class="ti-detail-item">
                                            <div class="ti-detail-label">Банк получателя</div>
                                            <div class="ti-detail-value">${transaction.recipientBank.name}</div>
                                        </div>
                                        <div class="ti-detail-item">
                                            <div class="ti-detail-label">Тип лица</div>
                                            <div class="ti-detail-value">${transaction.legalType.name}</div>
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

                    // Add click handler for edit button
                    const editButton = transactionEl.querySelector('#ti-edit');
                    editButton.addEventListener('click', () => {
                        editTransaction(transaction.transactionID);
                    });

                    transactionsList.appendChild(transactionEl);
                });

                // Update transactions summary
                updateTransactionsSummary(data.summary);

                // Initialize dashboard with dashboard data instead of transactions
                initDashboard(data.dashboards);
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



/* ---------------------------------------- */
/* -- Создание/редактирование транзакции -- */
/* ---------------------------------------- */

// Обработка модального окна создания транзакции

const transactionModal = document.getElementById('transaction-modal');
const transactionNew = document.getElementById('transaction-new');
const transactionForm = document.getElementById('transaction-form-element');
let currentEditingTransactionId = null;

// Открытие модального окна при клике на "Создать новую транзакцию"

transactionNew.addEventListener('click', () => {
    currentEditingTransactionId = null;
    transactionModal.style.display = 'flex';
    loadTransactionFormData();
    transactionForm.reset();
    document.querySelector('#transaction-modal h2').textContent = 'Создать новую транзакцию';
    document.querySelector('#transaction-form-element .auth-submit').textContent = 'Создать транзакцию';
});

// Закрытие модального окна при клике вне его содержимого

transactionModal.addEventListener('click', (e) => {
    if (e.target === transactionModal) {
        transactionModal.style.display = 'none';
        currentEditingTransactionId = null;
    }
});



// Функция для загрузки данных для формы (категории, банки, типы лиц)

function loadTransactionFormData() {
    const token = localStorage.getItem('authToken');
    const promises = [];
    
    // Загрузка категорий
    const categoriesPromise = fetch('http://localhost:7070/api/get_categories', {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
        .then(response => response.json())
        .then(data => {
            const categorySelect = document.getElementById('transaction-category');
            categorySelect.innerHTML = '<option value="">Выберите категорию</option>';
            data.categories.forEach(category => {
                const option = document.createElement('option');
                option.value = category.id;
                option.textContent = category.name;
                categorySelect.appendChild(option);
            });
            return data.categories;
        })
        .catch(error => {
            console.error('Ошибка загрузки категорий:', error);
            return [];
        });
    promises.push(categoriesPromise);

    // Загрузка статусов
    const statusesPromise = fetch('http://localhost:7070/api/get_statuses', {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
        .then(response => response.json())
        .then(data => {
            const statusSelect = document.getElementById('transaction-status');
            const filterStatusSelect = document.getElementById('filter-status');
            
            const defaultOption = '<option value="">Выберите статус</option>';
            statusSelect.innerHTML = defaultOption;
            filterStatusSelect.innerHTML = '<option value="">Все статусы</option>';
            
            data.statuses.forEach(status => {
                const option = document.createElement('option');
                option.value = status.id;
                option.textContent = status.name;
                
                // Клонируем опцию для обоих селектов
                statusSelect.appendChild(option.cloneNode(true));
                filterStatusSelect.appendChild(option);
            });
            return data.statuses;
        })
        .catch(error => {
            console.error('Ошибка загрузки статусов:', error);
            return [];
        });
    promises.push(statusesPromise);

    // Загрузка банков
    const banksPromise = fetch('http://localhost:7070/api/get_banks', {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
        .then(response => response.json())
        .then(data => {
            const senderBankSelect = document.getElementById('transaction-sender-bank');
            const recipientBankSelect = document.getElementById('transaction-recipient-bank');
            
            const defaultOption = '<option value="">Выберите банк</option>';
            senderBankSelect.innerHTML = defaultOption;
            recipientBankSelect.innerHTML = defaultOption;
            
            data.banks.forEach(bank => {
                const option = document.createElement('option');
                option.value = bank.id;
                option.textContent = bank.name;
                
                // Клонируем опцию для обоих селектов
                senderBankSelect.appendChild(option.cloneNode(true));
                recipientBankSelect.appendChild(option);
            });
            return data.banks;
        })
        .catch(error => {
            console.error('Ошибка загрузки банков:', error);
            return [];
        });
    promises.push(banksPromise);

    // Заполнение типов лиц (фиксированные значения)
    const legalTypeSelect = document.getElementById('transaction-legal-type');
    legalTypeSelect.innerHTML = '<option value="">Выберите тип лица</option>';
    
    const legalTypes = [
        { id: 1, name: 'Физическое лицо' },
        { id: 2, name: 'Юридическое лицо' }
    ];
    
    legalTypes.forEach(type => {
        const option = document.createElement('option');
        option.value = type.id;
        option.textContent = type.name;
        legalTypeSelect.appendChild(option);
    });

    return Promise.all(promises);
}

// Функция для редактирования транзакции
function editTransaction(transactionId) {
    currentEditingTransactionId = transactionId;
    transactionModal.style.display = 'flex';
    document.querySelector('#transaction-modal h2').textContent = 'Редактировать транзакцию';
    document.querySelector('#transaction-form-element .auth-submit').textContent = 'Сохранить изменения';

    // Находим элемент транзакции в DOM по точному атрибуту
    const transactionElement = document.querySelector(`.transaction-item[data-transaction-id="${transactionId}"]`);
    if (!transactionElement) {
        document.getElementById('transaction-error').textContent = 'Ошибка: транзакция не найдена';
        return;
    }

    // Получаем данные из DOM
    const typeElement = transactionElement.querySelector('.ti-type');
    const amountElement = transactionElement.querySelector('.ti-amount');
    const categoryElement = transactionElement.querySelector('.ti-category');
    const commentElement = transactionElement.querySelector('.ti-comment');
    const statusElement = transactionElement.querySelector('.ti-status');
    const detailsContent = transactionElement.querySelector('.ti-details-content');

    // Загружаем данные для селектов
    loadTransactionFormData()
        .then(() => {
            // Заполняем форму данными из DOM
            document.getElementById('transaction-type').value = typeElement.classList.contains('income') ? '1' : '2';
            document.getElementById('transaction-amount').value = parseFloat(amountElement.textContent.replace(/[^\d.-]/g, ''));
            document.getElementById('transaction-comment').value = commentElement.textContent;

            // Находим нужные опции в селектах по тексту
            const categorySelect = document.getElementById('transaction-category');
            const categoryOption = Array.from(categorySelect.options).find(option => 
                option.textContent === categoryElement.textContent
            );
            if (categoryOption) {
                categorySelect.value = categoryOption.value;
            }

            // Заполняем статус
            const statusSelect = document.getElementById('transaction-status');
            const statusOption = Array.from(statusSelect.options).find(option => 
                option.textContent === statusElement.textContent
            );
            if (statusOption) {
                statusSelect.value = statusOption.value;
            }

            // Заполняем данные из блока деталей
            if (detailsContent) {
                const detailItems = detailsContent.querySelectorAll('.ti-detail-item');
                detailItems.forEach(item => {
                    const label = item.querySelector('.ti-detail-label').textContent;
                    const value = item.querySelector('.ti-detail-value').textContent;

                    switch (label) {
                        case 'Банк':
                            const senderBankSelect = document.getElementById('transaction-sender-bank');
                            const senderBankOption = Array.from(senderBankSelect.options).find(option => 
                                option.textContent === value
                            );
                            if (senderBankOption) {
                                senderBankSelect.value = senderBankOption.value;
                            }
                            break;
                        case 'Счет списания':
                            document.getElementById('transaction-account').value = value;
                            break;
                        case 'Счет получателя':
                            document.getElementById('transaction-recipient').value = value;
                            break;
                        case 'ИНН получателя':
                            document.getElementById('transaction-tin').value = value;
                            break;
                        case 'Телефон получателя':
                            document.getElementById('transaction-phone').value = value;
                            break;
                        case 'Тип лица':
                            const legalTypeSelect = document.getElementById('transaction-legal-type');
                            const legalTypeOption = Array.from(legalTypeSelect.options).find(option => 
                                option.textContent === value
                            );
                            if (legalTypeOption) {
                                legalTypeSelect.value = legalTypeOption.value;
                            }
                            break;
                    }
                });
            }
        })
        .catch(error => {
            console.error('Ошибка:', error);
            document.getElementById('transaction-error').textContent = 'Ошибка при загрузке данных формы';
        });
}



// Обработка отправки формы создания/редактирования транзакции

transactionForm.addEventListener('submit', function(e) {
    e.preventDefault();
    
    const transactionError = document.getElementById('transaction-error');
    transactionError.textContent = '';

    const formData = new FormData(transactionForm);
    const phoneNumber = formData.get('recipientPhone');

    // Проверяем номер телефона
    if (!validatePhoneNumber(phoneNumber)) {
        transactionError.textContent = 'Номер телефона должен начинаться с 8 и содержать 11 цифр';
        return;
    }

    const transactionData = {
        type: parseInt(formData.get('type')),
        amount: parseFloat(formData.get('amount')),
        category: parseInt(formData.get('category')),
        senderBank: parseInt(formData.get('senderBank')),
        recipientBank: parseInt(formData.get('recipientBank')),
        accountNumber: formData.get('accountNumber'),
        recipientNumber: formData.get('recipientNumber'),
        recipientTIN: formData.get('recipientTIN'),
        recipientPhone: formatPhoneNumber(phoneNumber),
        legalType: parseInt(formData.get('legalType')),
        comment: formData.get('comment')
    };

    const token = localStorage.getItem('authToken');
    const url = currentEditingTransactionId 
        ? `http://localhost:7070/api/update_transaction/${currentEditingTransactionId}`
        : 'http://localhost:7070/api/create_transaction';
    const method = currentEditingTransactionId ? 'PUT' : 'POST';

    fetch(url, {
        method: method,
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(transactionData)
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                // Закрываем модальное окно
                transactionModal.style.display = 'none';
                // Очищаем форму
                transactionForm.reset();
                // Сбрасываем ID редактируемой транзакции
                currentEditingTransactionId = null;
                // Обновляем список транзакций
                getUserTransactions();
            } else {
                transactionError.textContent = data.error || 'Ошибка при сохранении транзакции';
            }
        })
        .catch(error => {
            console.error('Ошибка:', error);
            transactionError.textContent = 'Ошибка при выполнении запроса';
        });
});

// Загрузка данных для фильтров
function loadFilterData() {
    const token = localStorage.getItem('authToken');
    
    // Загрузка категорий
    fetch('http://localhost:7070/api/get_categories', {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
        .then(response => response.json())
        .then(data => {
            const categorySelect = document.getElementById('filter-category');
            data.categories.forEach(category => {
                const option = document.createElement('option');
                option.value = category.id;
                option.textContent = category.name;
                categorySelect.appendChild(option);
            });
        })
        .catch(error => {
            console.error('Ошибка загрузки категорий:', error);
        });

    // Загрузка статусов
    fetch('http://localhost:7070/api/get_statuses', {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
        .then(response => response.json())
        .then(data => {
            const statusSelect = document.getElementById('filter-status');
            data.statuses.forEach(status => {
                const option = document.createElement('option');
                option.value = status.id;
                option.textContent = status.name;
                statusSelect.appendChild(option);
            });
        })
        .catch(error => {
            console.error('Ошибка загрузки статусов:', error);
        });

    // Загрузка банков
    fetch('http://localhost:7070/api/get_banks', {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
        .then(response => response.json())
        .then(data => {
            const senderBankSelect = document.getElementById('filter-sender-bank');
            const recipientBankSelect = document.getElementById('filter-recipient-bank');
            
            data.banks.forEach(bank => {
                // Клонируем опцию для обоих селектов
                const senderOption = document.createElement('option');
                senderOption.value = bank.id;
                senderOption.textContent = bank.name;
                senderBankSelect.appendChild(senderOption);

                const recipientOption = document.createElement('option');
                recipientOption.value = bank.id;
                recipientOption.textContent = bank.name;
                recipientBankSelect.appendChild(recipientOption);
            });
        })
        .catch(error => {
            console.error('Ошибка загрузки банков:', error);
        });
}

// Функция для форматирования даты в SQL datetime формат
function formatDateToSQL(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    const seconds = String(date.getSeconds()).padStart(2, '0');
    return `${year}-${month}-${day}${hours}${minutes}${seconds}`;
}

// Функция для валидации номера телефона
function validatePhoneNumber(phone) {
    // Удаляем все нецифровые символы
    const cleanPhone = phone.replace(/\D/g, '');
    // Проверяем, что номер начинается с 8 и имеет правильную длину
    return cleanPhone.startsWith('8') && cleanPhone.length === 11;
}

// Функция для форматирования номера телефона
function formatPhoneNumber(phone) {
    // Удаляем все нецифровые символы
    const cleanPhone = phone.replace(/\D/g, '');
    // Если номер не начинается с 8, добавляем 8 в начало
    return cleanPhone.startsWith('8') ? cleanPhone : '8' + cleanPhone;
}

// Функция для построения query string из параметров фильтра
function buildQueryString(filters) {
    const queryParams = new URLSearchParams();
    
    // Получаем значения из select элементов
    const typeSelect = document.getElementById('filter-type');
    const categorySelect = document.getElementById('filter-category');
    const statusSelect = document.getElementById('filter-status');
    const senderBankSelect = document.getElementById('filter-sender-bank');
    const recipientBankSelect = document.getElementById('filter-recipient-bank');
    const legalTypeSelect = document.getElementById('filter-legal-type');
    
    // Форматируем даты в SQL datetime формат
    if (filters.dateFrom) {
        const dateFrom = new Date(filters.dateFrom);
        queryParams.append('transaction_date-gt', formatDateToSQL(dateFrom));
    }
    if (filters.dateTo) {
        const dateTo = new Date(filters.dateTo);
        queryParams.append('transaction_date-lw', formatDateToSQL(dateTo));
    }
    
    // Используем ID вместо текстовых значений с правильными именами параметров
    if (filters.type) {
        const typeId = typeSelect.value;
        queryParams.append('type_id-num', typeId);
    }
    if (filters.category) {
        const categoryId = categorySelect.value;
        queryParams.append('category_id-num', categoryId);
    }
    if (filters.amountFrom) queryParams.append('amount-gt', filters.amountFrom);
    if (filters.amountTo) queryParams.append('amount-lw', filters.amountTo);
    if (filters.status) {
        const statusId = statusSelect.value;
        queryParams.append('status_id-num', statusId);
    }
    if (filters.senderBank) {
        const senderBankId = senderBankSelect.value;
        queryParams.append('sender_bank_id-num', senderBankId);
    }
    if (filters.recipientBank) {
        const recipientBankId = recipientBankSelect.value;
        queryParams.append('recipient_bank_id-num', recipientBankId);
    }
    if (filters.account) queryParams.append('account_number-num', filters.account);
    if (filters.recipient) queryParams.append('recipient_number-num', filters.recipient);
    if (filters.tin) queryParams.append('recipient_tin-num', filters.tin);
    if (filters.phone) {
        const formattedPhone = formatPhoneNumber(filters.phone);
        if (validatePhoneNumber(formattedPhone)) {
            queryParams.append('recipient_phone-num', formattedPhone);
        }
    }
    if (filters.legalType) {
        const legalTypeId = legalTypeSelect.value;
        queryParams.append('legal_type_id-num', legalTypeId);
    }

    return queryParams.toString();
}

// Применение фильтров
function applyFilters() {
    const filters = {
        dateFrom: document.getElementById('filter-date-from').value,
        dateTo: document.getElementById('filter-date-to').value,
        type: document.getElementById('filter-type').value,
        category: document.getElementById('filter-category').value,
        amountFrom: document.getElementById('filter-amount-from').value,
        amountTo: document.getElementById('filter-amount-to').value,
        status: document.getElementById('filter-status').value,
        senderBank: document.getElementById('filter-sender-bank').value,
        recipientBank: document.getElementById('filter-recipient-bank').value,
        account: document.getElementById('filter-account').value,
        recipient: document.getElementById('filter-recipient').value,
        tin: document.getElementById('filter-tin').value,
        phone: document.getElementById('filter-phone').value,
        legalType: document.getElementById('filter-legal-type').value
    };

    const token = localStorage.getItem('authToken');
    const queryString = buildQueryString(filters);
    
    // Выводим строку запроса в консоль
    console.log('Query string:', queryString);

    fetch(`http://localhost:7070/api/get_transactions?${queryString}`, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
        .then(response => response.json())
        .then(data => {
            const transactionsList = document.getElementById('transactions-list');
            transactionsList.innerHTML = '';

            if (data.transactions && Array.isArray(data.transactions)) {
                data.transactions.forEach(transaction => {
                    // Используем существующую функцию для отображения транзакций
                    displayTransaction(transaction);
                });
            }
        })
        .catch(error => {
            console.error('Ошибка:', error);
            document.getElementById('transactions-error').textContent = 'Ошибка при загрузке транзакций';
        });
}

// Инициализация фильтров
document.addEventListener('DOMContentLoaded', () => {
    loadFilterData();

    // Обработчик для кнопки "Применить параметры"
    document.getElementById('apply-button').addEventListener('click', applyFilters);

    // Обработчик для кнопки "Доп. параметры"
    const filterDetailsToggle = document.querySelector('#filter-parameter .ti-details-toggle');
    const filterDetailsContent = document.querySelector('#filter-parameter .ti-details-content');
    
    filterDetailsToggle.addEventListener('click', () => {
        filterDetailsContent.classList.toggle('active');
        filterDetailsToggle.classList.toggle('active');
        filterDetailsToggle.textContent = filterDetailsToggle.classList.contains('active') 
            ? 'Скрыть параметры' 
            : 'Доп. параметры';
    });

    // Добавляем обработчики для полей ввода телефона
    const phoneInputs = document.querySelectorAll('input[type="tel"]');
    phoneInputs.forEach(input => {
        input.addEventListener('input', function(e) {
            // Удаляем все нецифровые символы
            let value = e.target.value.replace(/\D/g, '');
            // Если номер не начинается с 8, добавляем 8
            if (value && !value.startsWith('8')) {
                value = '8' + value;
            }
            // Ограничиваем длину до 11 цифр
            value = value.slice(0, 11);
            e.target.value = value;
        });

        input.addEventListener('blur', function(e) {
            const value = e.target.value;
            if (value && !validatePhoneNumber(value)) {
                e.target.value = '';
            }
        });
    });
});






/* --------------------------------- */
/* -- Генерация сводки и дашборда -- */
/* --------------------------------- */


// Функция для обновления сводки по транзакциям
function updateTransactionsSummary(data) {
    const summaryValues = document.querySelectorAll('.summary-value');
    if (summaryValues.length >= 4) {
        summaryValues[0].textContent = data.total_count.toLocaleString('ru-RU');
        summaryValues[1].textContent = data.total_income.toLocaleString('ru-RU') + 'р';
        summaryValues[2].textContent = data.total_expense.toLocaleString('ru-RU') + 'р';
        summaryValues[3].textContent = data.balance.toLocaleString('ru-RU') + 'р';
    }
}


// Dashboard functionality
document.addEventListener('DOMContentLoaded', initDashboard);

// Store chart instances
let charts = {};
let currentPeriod = 'monthly'; // Default period

// Main initialization function
function initDashboard(dashboards) {
    // Only initialize if the dashboard container exists
    const dashboardContainer = document.getElementById('dashboard-container');
    if (!dashboardContainer) return;

    // Load Chart.js from CDN
    loadScript('https://cdn.jsdelivr.net/npm/chart.js', () => {
        // Generate mock data structure with empty values
        const data = generateMockData(dashboards);
        
        // Set monthly period as active
        currentPeriod = 'monthly';
        document.querySelector(`.period-button[data-period="monthly"]`).classList.add('active');
        
        // Initialize all charts
        charts.transactionsByPeriod = createTransactionsByPeriodChart(data);
        charts.debitTransactions = createDebitTransactionsChart(data);
        charts.creditTransactions = createCreditTransactionsChart(data);
        charts.incomeExpenseComparison = createIncomeExpenseComparisonChart(data);
        charts.transactionStatus = createTransactionStatusChart(data);
        charts.bankStatistics = createBankStatisticsChart(data);
        charts.expenseCategories = createExpenseCategoriesChart(data);
        charts.incomeCategories = createIncomeCategoriesChart(data);

        // Add period selector event listeners
        setupPeriodSelector(data);

        // Add window resize handler
        window.addEventListener('resize', handleResize);
    });
}

// Setup period selector functionality
function setupPeriodSelector(data) {
    const buttons = document.querySelectorAll('.period-button');
    buttons.forEach(button => {
        button.addEventListener('click', () => {
            // Update active button
            buttons.forEach(btn => btn.classList.remove('active'));
            button.classList.add('active');
            
            // Update current period
            currentPeriod = button.dataset.period;
            
            // Update charts
            updateChartsForPeriod(data);
        });
    });
}

// Update charts based on selected period
function updateChartsForPeriod(data) {
    // Update transactions by period chart
    updateChartData(charts.transactionsByPeriod, data.transactionsByPeriod[currentPeriod]);

    // Update debit transactions chart
    updateChartData(charts.debitTransactions, data.transactionsByType.debit[currentPeriod]);

    // Update credit transactions chart
    updateChartData(charts.creditTransactions, data.transactionsByType.credit[currentPeriod]);

    // Update income vs expense chart
    updateIncomeExpenseChart(charts.incomeExpenseComparison, data.incomeVsExpense[currentPeriod]);
}

// Helper function to update chart data
function updateChartData(chart, newData) {
    if (!newData || !newData.labels || !newData.values) {
        console.error('Invalid data format for chart update:', newData);
        return;
    }
    
    chart.data.labels = newData.labels;
    chart.data.datasets[0].data = newData.values;
    
    // Update scales if needed
    if (chart.options.scales.x) {
        chart.options.scales.x.maxTicksLimit = Math.min(6, newData.labels.length);
    }
    
    chart.update();
}

// Helper function to update income vs expense chart
function updateIncomeExpenseChart(chart, newData) {
    if (!newData || !newData.labels || !newData.income || !newData.expense) {
        console.error('Invalid data format for income/expense chart update:', newData);
        return;
    }
    
    chart.data.labels = newData.labels;
    chart.data.datasets[0].data = newData.income;
    chart.data.datasets[1].data = newData.expense;
    
    // Update scales if needed
    if (chart.options.scales.x) {
        chart.options.scales.x.maxTicksLimit = Math.min(6, newData.labels.length);
    }
    
    chart.update();
}

// Helper function to get period label
function getPeriodLabel(period) {
    const labels = {
        weekly: 'Еженедельно',
        monthly: 'Ежемесячно',
        quarterly: 'Ежеквартально',
        yearly: 'Ежегодно'
    };
    return labels[period];
}

// Handle window resize
function handleResize() {
    Object.values(charts).forEach(chart => {
        if (chart) {
            chart.resize();
        }
    });
}

// Helper function to load external scripts
function loadScript(url, callback) {
    const script = document.createElement('script');
    script.src = url;
    script.onload = callback;
    document.head.appendChild(script);
}

// Function to generate mock data structure for charts
function generateMockData(dashboards) {
    const dashboard1 = dashboards["Dashboard 1"];
    const dashboard2 = dashboards["Dashboard 2"];
    const dashboard3 = dashboards["Dashboard 3"];
    const dashboard4 = dashboards["Dashboard 4"];
    const dashboard5 = dashboards["Dashboard 5"];
    const dashboard6 = dashboards["Dashboard 6"];

    return {
        transactionsByPeriod: {
            weekly: {
                labels: dashboard1.weekly[0],
                values: dashboard1.weekly[1]
            },
            monthly: {
                labels: dashboard1.monthly[0],
                values: dashboard1.monthly[1]
            },
            quarterly: {
                labels: dashboard1.quarterly[0],
                values: dashboard1.quarterly[1]
            },
            yearly: {
                labels: dashboard1.yearly[0],
                values: dashboard1.yearly[1]
            }
        },
        transactionsByType: {
            debit: {
                weekly: {
                    labels: dashboard2.weekly[0],
                    values: dashboard2.weekly[1]
                },
                monthly: {
                    labels: dashboard2.monthly[0],
                    values: dashboard2.monthly[1]
                },
                quarterly: {
                    labels: dashboard2.quarterly[0],
                    values: dashboard2.quarterly[1]
                },
                yearly: {
                    labels: dashboard2.yearly[0],
                    values: dashboard2.yearly[1]
                }
            },
            credit: {
                weekly: {
                    labels: dashboard2.weekly[0],
                    values: dashboard2.weekly[2]
                },
                monthly: {
                    labels: dashboard2.monthly[0],
                    values: dashboard2.monthly[2]
                },
                quarterly: {
                    labels: dashboard2.quarterly[0],
                    values: dashboard2.quarterly[2]
                },
                yearly: {
                    labels: dashboard2.yearly[0],
                    values: dashboard2.yearly[2]
                }
            }
        },
        incomeVsExpense: {
            weekly: {
                labels: dashboard3.weekly[0],
                income: dashboard3.weekly[1],
                expense: dashboard3.weekly[2]
            },
            monthly: {
                labels: dashboard3.monthly[0],
                income: dashboard3.monthly[1],
                expense: dashboard3.monthly[2]
            },
            quarterly: {
                labels: dashboard3.quarterly[0],
                income: dashboard3.quarterly[1],
                expense: dashboard3.quarterly[2]
            },
            yearly: {
                labels: dashboard3.yearly[0],
                income: dashboard3.yearly[1],
                expense: dashboard3.yearly[2]
            }
        },
        transactionStatus: {
            completed: dashboard4["Completed Transactions"],
            cancelled: dashboard4["Cancelled Transactions"]
        },
        bankStatistics: {
            sender: dashboard5.BankSenders,
            receiver: dashboard5.BankReceivers
        },
        categories: {
            expense: dashboard6["Expenses Categories"],
            income: dashboard6["Income Categories"]
        }
    };
}

// Create chart for transactions by period (week/month/quarter/year)
function createTransactionsByPeriodChart(data) {
    const ctx = document.getElementById('transactions-by-period-chart').getContext('2d');
    
    const chart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: data.transactionsByPeriod.monthly.labels,
            datasets: [{
                label: 'Количество транзакций',
                data: data.transactionsByPeriod.monthly.values,
                backgroundColor: 'rgba(75, 192, 192, 0.7)',
                borderColor: 'rgba(75, 192, 192, 1)',
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: false
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    title: {
                        display: true,
                        text: 'Количество транзакций'
                    },
                    ticks: {
                        maxRotation: 0,
                        autoSkip: true,
                        maxTicksLimit: 5
                    }
                },
                x: {
                    title: {
                        display: true,
                        text: 'Период'
                    },
                    ticks: {
                        maxRotation: 45,
                        autoSkip: true,
                        maxTicksLimit: 6
                    }
                }
            }
        }
    });

    return chart;
}

// Create chart for debit transactions 
function createDebitTransactionsChart(data) {
    const ctx = document.getElementById('debit-transactions-chart').getContext('2d');
    
    const chart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: data.transactionsByType.debit.monthly.labels,
            datasets: [{
                label: 'Дебетовые транзакции',
                data: data.transactionsByType.debit.monthly.values,
                backgroundColor: 'rgba(255, 99, 132, 0.7)',
                borderColor: 'rgba(255, 99, 132, 1)',
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: false
                },
                title: {
                    display: true,
                    text: 'Дебетовые транзакции',
                    font: {
                        size: 16
                    },
                    padding: 20
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    title: {
                        display: true,
                        text: 'Количество транзакций'
                    },
                    ticks: {
                        maxRotation: 0,
                        autoSkip: true,
                        maxTicksLimit: 5
                    }
                },
                x: {
                    title: {
                        display: true,
                        text: 'Период'
                    },
                    ticks: {
                        maxRotation: 45,
                        autoSkip: true,
                        maxTicksLimit: 6
                    }
                }
            }
        }
    });

    return chart;
}

// Create chart for credit transactions
function createCreditTransactionsChart(data) {
    const ctx = document.getElementById('credit-transactions-chart').getContext('2d');
    
    const chart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: data.transactionsByType.credit.monthly.labels,
            datasets: [{
                label: 'Кредитовые транзакции',
                data: data.transactionsByType.credit.monthly.values,
                backgroundColor: 'rgba(54, 162, 235, 0.7)',
                borderColor: 'rgba(54, 162, 235, 1)',
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: false
                },
                title: {
                    display: true,
                    text: 'Кредитовые транзакции',
                    font: {
                        size: 16
                    },
                    padding: 20
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    title: {
                        display: true,
                        text: 'Количество транзакций'
                    },
                    ticks: {
                        maxRotation: 0,
                        autoSkip: true,
                        maxTicksLimit: 5
                    }
                },
                x: {
                    title: {
                        display: true,
                        text: 'Период'
                    },
                    ticks: {
                        maxRotation: 45,
                        autoSkip: true,
                        maxTicksLimit: 6
                    }
                }
            }
        }
    });

    return chart;
}

// Create chart for income vs expense comparison
function createIncomeExpenseComparisonChart(data) {
    const ctx = document.getElementById('income-expense-comparison-chart').getContext('2d');
    
    const chart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: data.incomeVsExpense.monthly.labels,
            datasets: [{
                label: 'Поступления',
                data: data.incomeVsExpense.monthly.income,
                backgroundColor: 'rgba(75, 192, 192, 0.7)',
                borderColor: 'rgba(75, 192, 192, 1)',
                borderWidth: 1
            }, {
                label: 'Расходы',
                data: data.incomeVsExpense.monthly.expense,
                backgroundColor: 'rgba(255, 99, 132, 0.7)',
                borderColor: 'rgba(255, 99, 132, 1)',
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'top',
                    labels: {
                        boxWidth: 12,
                        padding: 10
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    title: {
                        display: true,
                        text: 'Сумма (₽)'
                    },
                    ticks: {
                        maxRotation: 0,
                        autoSkip: true,
                        maxTicksLimit: 5
                    }
                },
                x: {
                    title: {
                        display: true,
                        text: 'Период'
                    },
                    ticks: {
                        maxRotation: 45,
                        autoSkip: true,
                        maxTicksLimit: 6
                    }
                }
            }
        }
    });

    return chart;
}

// Create chart for transaction status
function createTransactionStatusChart(data) {
    const ctx = document.getElementById('transaction-status-chart').getContext('2d');
    
    const chart = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: ['Завершено', 'Отменено'],
            datasets: [{
                data: [data.transactionStatus.completed, data.transactionStatus.cancelled],
                backgroundColor: [
                    'rgba(75, 192, 192, 0.7)',
                    'rgba(255, 99, 132, 0.7)'
                ],
                borderColor: [
                    'rgba(75, 192, 192, 1)',
                    'rgba(255, 99, 132, 1)'
                ],
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'right',
                    labels: {
                        boxWidth: 12,
                        padding: 10
                    }
                }
            }
        }
    });

    return chart;
}

// Create chart for bank statistics
function createBankStatisticsChart(data) {
    const ctx = document.getElementById('bank-statistics-chart').getContext('2d');
    
    const chart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: Object.keys(data.bankStatistics.sender),
            datasets: [{
                label: 'Отправитель',
                data: Object.values(data.bankStatistics.sender),
                backgroundColor: 'rgba(54, 162, 235, 0.7)',
                borderColor: 'rgba(54, 162, 235, 1)',
                borderWidth: 1
            }, {
                label: 'Получатель',
                data: Object.values(data.bankStatistics.receiver),
                backgroundColor: 'rgba(255, 159, 64, 0.7)',
                borderColor: 'rgba(255, 159, 64, 1)',
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'top',
                    labels: {
                        boxWidth: 12,
                        padding: 10
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    title: {
                        display: true,
                        text: 'Количество транзакций'
                    },
                    ticks: {
                        maxRotation: 0,
                        autoSkip: true,
                        maxTicksLimit: 5
                    }
                },
                x: {
                    ticks: {
                        maxRotation: 45,
                        autoSkip: true,
                        maxTicksLimit: 6
                    }
                }
            }
        }
    });

    return chart;
}

// Create chart for expense categories
function createExpenseCategoriesChart(data) {
    const ctx = document.getElementById('expense-categories-chart').getContext('2d');
    
    const chart = new Chart(ctx, {
        type: 'pie',
        data: {
            labels: Object.keys(data.categories.expense),
            datasets: [{
                data: Object.values(data.categories.expense),
                backgroundColor: [
                    'rgba(255, 99, 132, 0.7)',
                    'rgba(54, 162, 235, 0.7)',
                    'rgba(255, 206, 86, 0.7)',
                    'rgba(75, 192, 192, 0.7)',
                    'rgba(153, 102, 255, 0.7)',
                    'rgba(255, 159, 64, 0.7)',
                    'rgba(199, 199, 199, 0.7)'
                ],
                borderColor: [
                    'rgba(255, 99, 132, 1)',
                    'rgba(54, 162, 235, 1)',
                    'rgba(255, 206, 86, 1)',
                    'rgba(75, 192, 192, 1)',
                    'rgba(153, 102, 255, 1)',
                    'rgba(255, 159, 64, 1)',
                    'rgba(199, 199, 199, 1)'
                ],
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'right',
                    labels: {
                        boxWidth: 12,
                        padding: 10
                    }
                }
            }
        }
    });

    return chart;
}

// Create chart for income categories
function createIncomeCategoriesChart(data) {
    const ctx = document.getElementById('income-categories-chart').getContext('2d');
    
    const chart = new Chart(ctx, {
        type: 'pie',
        data: {
            labels: Object.keys(data.categories.income),
            datasets: [{
                data: Object.values(data.categories.income),
                backgroundColor: [
                    'rgba(75, 192, 192, 0.7)',
                    'rgba(54, 162, 235, 0.7)',
                    'rgba(255, 206, 86, 0.7)',
                    'rgba(255, 99, 132, 0.7)',
                    'rgba(153, 102, 255, 0.7)'
                ],
                borderColor: [
                    'rgba(75, 192, 192, 1)',
                    'rgba(54, 162, 235, 1)',
                    'rgba(255, 206, 86, 1)',
                    'rgba(255, 99, 132, 1)',
                    'rgba(153, 102, 255, 1)'
                ],
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'right',
                    labels: {
                        boxWidth: 12,
                        padding: 10
                    }
                }
            }
        }
    });

    return chart;
}