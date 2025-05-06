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
    console.log('Resetting all filters...');
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



// Функция для отображения списка транзакций
function displayTransactions(transactions, summary, dashboards) {
    const transactionsList = document.getElementById('transactions-list');
    transactionsList.innerHTML = '';

    if (transactions && Array.isArray(transactions)) {
        // Reverse the array to show newest transactions first
        transactions.reverse().forEach(transaction => {
            const transactionEl = document.createElement('div');
            transactionEl.className = 'transaction-item';
            transactionEl.setAttribute('data-transaction-id', transaction.transactionID);

            // Format date
            // Преобразуем массив даты в читаемый формат на русском языке
            const dateArray = transaction.transactionDate;
            const year = dateArray[0];
            const month = dateArray[1];
            const day = dateArray[2];
            const hours = String(dateArray[3]).padStart(2, '0');
            const minutes = String(dateArray[4]).padStart(2, '0');

            // Массив названий месяцев в родительном падеже
            const months = [
                'января', 'февраля', 'марта', 'апреля', 'мая', 'июня',
                'июля', 'августа', 'сентября', 'октября', 'ноября', 'декабря'
            ];

            const formattedDate = `${day} ${months[month - 1]} ${year} в ${hours}:${minutes}`;

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
                ${transaction.status.name === 'Новая' ? `
                <div class="ti-buttons">
                    <div id="ti-edit" class="ti__button"></div>
                    <div id="ti-apply" class="ti__button"></div>
                    <div id="ti-delete" class="ti__button"></div>
                </div>
                ` : ''}
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

            // Add click handler for edit button only if it exists
            const editButton = transactionEl.querySelector('#ti-edit');
            if (editButton) {
                editButton.addEventListener('click', () => {
                    editTransaction(transaction.transactionID);
                });
            }

            // Add click handler for confirm button
            const confirmButton = transactionEl.querySelector('#ti-apply');
            if (confirmButton) {
                confirmButton.addEventListener('click', () => {
                    confirmTransaction(transaction.transactionID);
                });
            }

            // Add click handler for delete button
            const deleteButton = transactionEl.querySelector('#ti-delete');
            if (deleteButton) {
                deleteButton.addEventListener('click', () => {
                    deleteTransaction(transaction.transactionID);
                });
            }

            transactionsList.appendChild(transactionEl);
        });

        // Update transactions summary if provided
        if (summary) {
            updateTransactionsSummary(summary);
        }

        // Initialize dashboard if provided
        if (dashboards) {
            initDashboard(dashboards);
        }
    }
}

// Обновляем функцию getUserTransactions
function getUserTransactions() {
    const transactionsError = document.getElementById('transactions-error');
    if (transactionsError) {
        transactionsError.textContent = '';
    }

    // Переключаем на таб "Сводка по транзакциям"
    document.getElementById('button-1').checked = true;

    const token = localStorage.getItem('authToken');

    console.log('Fetching transactions without filters...');

    fetch('http://localhost:7070/api/get_transactions', {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
        .then(response => response.json())
        .then(data => {
            console.log('Received data:', {
                transactions: data.transactions,
                summary: data.summary,
                dashboards: data.dashboards
            });
            displayTransactions(data.transactions, data.summary, data.dashboards);
            // Устанавливаем период "Ежемесячно"
            const monthlyButton = document.querySelector('.period-button[data-period="monthly"]');
            if (monthlyButton) monthlyButton.click();
        })
        .catch(error => {
            console.error('Ошибка:', error);
            transactionsError.textContent = error.message || 'Ошибка при загрузке транзакций';
        });
}

// Обновляем функцию applyFilters
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

    console.log('Applying filters with parameters:', filters);

    // Переключаем на таб "Сводка по транзакциям"
    document.getElementById('button-1').checked = true;

    const token = localStorage.getItem('authToken');
    const queryString = buildQueryString(filters);

    console.log('Generated query string:', queryString);

    fetch(`http://localhost:7070/api/get_transactions?${queryString}`, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
        .then(response => response.json())
        .then(data => {
            console.log('Received filtered data:', {
                transactions: data.transactions,
                summary: data.summary,
                dashboards: data.dashboards
            });
            displayTransactions(data.transactions, data.summary, data.dashboards);
            // Устанавливаем период "Ежемесячно"
            const monthlyButton = document.querySelector('.period-button[data-period="monthly"]');
            if (monthlyButton) {
                monthlyButton.click();
            }
        })
        .catch(error => {
            console.error('Ошибка:', error);
            document.getElementById('transactions-error').textContent = 'Ошибка при загрузке транзакций';
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
    
    // Устанавливаем текущую дату и время по умолчанию
    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    const day = String(now.getDate()).padStart(2, '0');
    const hours = String(now.getHours()).padStart(2, '0');
    const minutes = String(now.getMinutes()).padStart(2, '0');
    document.getElementById('transaction-date').value = `${year}-${month}-${day}T${hours}:${minutes}`;
    
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



// Функция для загрузки всех справочных данных
function loadReferenceData() {
    const token = localStorage.getItem('authToken');
    return fetch('http://localhost:7070/api/reference_data', {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
    .then(response => response.json())
    .then(data => {
        // Сохраняем данные в localStorage для повторного использования
        localStorage.setItem('referenceData', JSON.stringify(data));
        return data;
    })
    .catch(error => {
        console.error('Ошибка загрузки справочных данных:', error);
        return null;
    });
}

// Функция для загрузки данных для формы (категории, банки, типы лиц)
function loadTransactionFormData() {
    const token = localStorage.getItem('authToken');
    
    // Пытаемся получить данные из localStorage
    const cachedData = localStorage.getItem('referenceData');
    if (cachedData) {
        const data = JSON.parse(cachedData);
        populateTransactionFormData(data);
        return Promise.resolve(data);
    }

    // Если данных нет в кэше, загружаем их
    return loadReferenceData().then(data => {
        if (data) {
            populateTransactionFormData(data);
        }
        return data;
    });
}

// Функция для заполнения формы справочными данными
function populateTransactionFormData(data) {
    // Заполнение категорий
    const categorySelect = document.getElementById('transaction-category');
    categorySelect.innerHTML = '<option value="">Выберите категорию</option>';
    data.categories.forEach(category => {
        const option = document.createElement('option');
        option.value = category.categoryID;
        option.textContent = category.name;
        categorySelect.appendChild(option);
    });

    // Заполнение статусов
    const statusSelect = document.getElementById('transaction-status');
    const filterStatusSelect = document.getElementById('filter-status');
    
    const defaultOption = '<option value="">Выберите статус</option>';
    statusSelect.innerHTML = defaultOption;
    filterStatusSelect.innerHTML = '<option value="">Все статусы</option>';
    
    data.statuses.forEach(status => {
        const option = document.createElement('option');
        option.value = status.statusID;
        option.textContent = status.name;
        
        // Клонируем опцию для обоих селектов
        statusSelect.appendChild(option.cloneNode(true));
        filterStatusSelect.appendChild(option);
    });

    // Заполнение банков
    const senderBankSelect = document.getElementById('transaction-sender-bank');
    const recipientBankSelect = document.getElementById('transaction-recipient-bank');
    
    const defaultBankOption = '<option value="">Выберите банк</option>';
    senderBankSelect.innerHTML = defaultBankOption;
    recipientBankSelect.innerHTML = defaultBankOption;
    
    data.banks.forEach(bank => {
        const option = document.createElement('option');
        option.value = bank.bankID;
        option.textContent = bank.name;
        
        // Клонируем опцию для обоих селектов
        senderBankSelect.appendChild(option.cloneNode(true));
        recipientBankSelect.appendChild(option);
    });

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
}

// Функция для получения и форматирования даты из карточки транзакции
function getFormattedTransactionDate(transactionElement) {
    // Получаем дату из карточки
    let dateText = '';
    const infoGroups = transactionElement.querySelectorAll('.ti-info-group');
    for (const group of infoGroups) {
        const label = group.querySelector('.ti-info-label');
        if (label && label.textContent.trim() === 'Дата') {
            const value = group.querySelector('.ti-info-value');
            if (value) {
                dateText = value.textContent.trim();
                break;
            }
        }
    }

    // Преобразуем дату из формата "1 июня 2024 в 14:30" в формат "2024-06-01T14:30"
    try {
        if (dateText) {
            const dateRegex = /(\d{1,2}) (\S+) (\d{4}) в (\d{2}):(\d{2})/;
            const months = [
                'января', 'февраля', 'марта', 'апреля', 'мая', 'июня',
                'июля', 'августа', 'сентября', 'октября', 'ноября', 'декабря'
            ];
            const match = dateText.match(dateRegex);
            if (match) {
                const day = match[1].padStart(2, '0');
                const month = (months.indexOf(match[2]) + 1).toString().padStart(2, '0');
                const year = match[3];
                const hours = match[4];
                const minutes = match[5];
                return `${year}-${month}-${day}T${hours}:${minutes}`;
            }
        }
    } catch (err) {
        return null;
    }
    return null;
}

// Функция для редактирования транзакции
function editTransaction(transactionId) {
    console.log('Editing transaction with ID:', transactionId);
    currentEditingTransactionId = transactionId;
    transactionModal.style.display = 'flex';
    document.querySelector('#transaction-modal h2').textContent = 'Редактировать транзакцию';
    document.querySelector('#transaction-form-element .auth-submit').textContent = 'Сохранить изменения';

    // Находим элемент транзакции в DOM по точному атрибуту
    const transactionElement = document.querySelector(`.transaction-item[data-transaction-id="${transactionId}"]`);
    if (!transactionElement) {
        console.error('Transaction element not found in DOM');
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

    // Получаем отформатированную дату
    const formattedDate = getFormattedTransactionDate(transactionElement);
    console.log('Retrieved transaction data:', {
        type: typeElement?.textContent,
        amount: amountElement?.textContent,
        category: categoryElement?.textContent,
        comment: commentElement?.textContent,
        status: statusElement?.textContent,
        date: formattedDate
    });

    // Загружаем данные для селектов
    loadTransactionFormData()
        .then(() => {
            // Заполняем форму данными из DOM
            document.getElementById('transaction-type').value = typeElement.classList.contains('income') ? '1' : '2';
            document.getElementById('transaction-amount').value = parseFloat(amountElement.textContent.replace(/[^\d.-]/g, ''));
            document.getElementById('transaction-comment').value = commentElement.textContent;

            // Устанавливаем дату в поле формы
            if (formattedDate) {
                document.getElementById('transaction-date').value = formattedDate;
            }

            // Находим нужные опции в селектах по тексту и устанавливаем значения
            const categorySelect = document.getElementById('transaction-category');
            const categoryOption = Array.from(categorySelect.options).find(option => 
                option.textContent.trim() === categoryElement.textContent.trim()
            );
            if (categoryOption) {
                categorySelect.value = categoryOption.value;
            }

            // Заполняем статус
            const statusSelect = document.getElementById('transaction-status');
            const statusOption = Array.from(statusSelect.options).find(option => 
                option.textContent.trim() === statusElement.textContent.trim()
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
                        case 'Банк отправителя':
                            const senderBankSelect = document.getElementById('transaction-sender-bank');
                            const senderBankOption = Array.from(senderBankSelect.options).find(option => 
                                option.textContent === value
                            );
                            if (senderBankOption) {
                                senderBankSelect.value = senderBankOption.value;
                            }
                            break;
                        case 'Банк получателя':
                            const recipientBankSelect = document.getElementById('transaction-recipient-bank');
                            const recipientBankOption = Array.from(recipientBankSelect.options).find(option => 
                                option.textContent === value
                            );    
                            if (recipientBankOption) {
                                recipientBankSelect.value = recipientBankOption.value;
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
            console.error('Error loading form data:', error);
            document.getElementById('transaction-error').textContent = 'Ошибка при загрузке данных формы';
        });
}



// Утилиты для работы с телефонными номерами
const PhoneNumberUtils = {
    // Очистка номера от нецифровых символов, сохраняя знак +
    clean: function(phone) {
        if (!phone) return '';
        // Сохраняем знак + если он есть в начале
        const hasPlus = phone.startsWith('+');
        // Удаляем все нецифровые символы
        const cleaned = phone.replace(/\D/g, '');
        // Возвращаем номер с + если он был в начале
        return hasPlus ? '+' + cleaned : cleaned;
    },

    // Проверка валидности номера
    isValid: function(phone) {
        // Проверяем, что номер начинается с 8 или +7
        if (!(phone.startsWith('8') || phone.startsWith('+7'))) {
            return false;
        }
        // Проверяем длину после удаления нецифровых символов
        const cleanPhone = this.clean(phone);
        return cleanPhone.length === (cleanPhone.startsWith('+') ? 12 : 11);
    },

    // Получение сообщения об ошибке
    getErrorMessage: function() {
        return 'Номер телефона должен начинаться с 8 или +7 и содержать 11 цифр';
    },

    // Установка валидации для поля ввода
    setupValidation: function(input) {
        // Создаем элемент для отображения ошибки
        const errorElement = document.createElement('div');
        errorElement.className = 'input-error';
        input.parentNode.appendChild(errorElement);

        input.addEventListener('input', (e) => {
            const value = e.target.value;
            if (value && !this.isValid(value)) {
                errorElement.textContent = this.getErrorMessage();
                errorElement.classList.add('active');
                input.classList.add('error');
            } else {
                errorElement.classList.remove('active');
                input.classList.remove('error');
            }
        });

        input.addEventListener('blur', (e) => {
            const value = e.target.value;
            if (value && !this.isValid(value)) {
                errorElement.textContent = this.getErrorMessage();
                errorElement.classList.add('active');
                input.classList.add('error');
            } else {
                errorElement.classList.remove('active');
                input.classList.remove('error');
            }
        });
    }
};

// Обработка отправки формы создания/редактирования транзакции
transactionForm.addEventListener('submit', function(e) {
    e.preventDefault();
    
    const transactionError = document.getElementById('transaction-error');
    transactionError.textContent = '';

    const formData = new FormData(transactionForm);
    const phoneNumber = formData.get('recipientPhone');

    // Проверяем номер телефона
    if (!PhoneNumberUtils.isValid(phoneNumber)) {
        // Находим поле ввода телефона и показываем ошибку рядом с ним
        const phoneInput = document.getElementById('transaction-phone');
        const errorElement = phoneInput.parentNode.querySelector('.input-error');
        if (errorElement) {
            errorElement.textContent = PhoneNumberUtils.getErrorMessage();
            errorElement.classList.add('active');
            phoneInput.classList.add('error');
        }
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
        recipientPhone: PhoneNumberUtils.clean(phoneNumber),
        legalType: parseInt(formData.get('legalType')),
        comment: formData.get('comment'),
        status: parseInt(formData.get('status')) || 1,
        transactionDate: formData.get('transactionDate')
    };

    console.log('Sending transaction data to backend:', transactionData);

    const token = localStorage.getItem('authToken');
    const url = currentEditingTransactionId 
        ? `http://localhost:7070/api/update_transaction/${currentEditingTransactionId}`
        : 'http://localhost:7070/api/create_transaction';
    const method = currentEditingTransactionId ? 'PUT' : 'POST';

    fetch(url, {
        method: method,
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(transactionData)
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            // Закрываем модальное окно
            document.getElementById('transaction-modal').style.display = 'none';
            // Обновляем список транзакций
            getUserTransactions();
        } else {
            transactionError.textContent = data.message || 'Ошибка при сохранении транзакции';
        }
    })
    .catch(error => {
        console.error('Error:', error);
        transactionError.textContent = 'Ошибка при сохранении транзакции';
    });
});

// Загрузка данных для фильтров
function loadFilterData() {
    // Пытаемся получить данные из localStorage
    const cachedData = localStorage.getItem('referenceData');
    if (cachedData) {
        const data = JSON.parse(cachedData);
        populateFilterData(data);
        return;
    }

    // Если данных нет в кэше, загружаем их
    loadReferenceData().then(data => {
        if (data) {
            populateFilterData(data);
        }
    });
}

// Функция для заполнения фильтров справочными данными
function populateFilterData(data) {
    // Заполнение категорий
    const categorySelect = document.getElementById('filter-category');
    categorySelect.innerHTML = '<option value="">Все категории</option>';
    data.categories.forEach(category => {
        const option = document.createElement('option');
        option.value = category.categoryID;
        option.textContent = category.name;
        categorySelect.appendChild(option);
    });

    // Заполнение статусов
    const statusSelect = document.getElementById('filter-status');
    statusSelect.innerHTML = '<option value="">Все статусы</option>';
    data.statuses.forEach(status => {
        const option = document.createElement('option');
        option.value = status.statusID;
        option.textContent = status.name;
        statusSelect.appendChild(option);
    });

    // Заполнение банков
    const senderBankSelect = document.getElementById('filter-sender-bank');
    const recipientBankSelect = document.getElementById('filter-recipient-bank');
    
    senderBankSelect.innerHTML = '<option value="">Все банки</option>';
    recipientBankSelect.innerHTML = '<option value="">Все банки</option>';
    
    data.banks.forEach(bank => {
        // Клонируем опцию для обоих селектов
        const senderOption = document.createElement('option');
        senderOption.value = bank.bankID;
        senderOption.textContent = bank.name;
        senderBankSelect.appendChild(senderOption);

        const recipientOption = document.createElement('option');
        recipientOption.value = bank.bankID;
        recipientOption.textContent = bank.name;
        recipientBankSelect.appendChild(recipientOption);
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
    return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`;
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
        // Преобразуем строку datetime-local в объект Date
        const dateFrom = new Date(filters.dateFrom);
        // Проверяем, что дата валидна
        if (!isNaN(dateFrom.getTime())) {
            queryParams.append('transaction_date-gt', formatDateToSQL(dateFrom));
        }
    }
    if (filters.dateTo) {
        // Преобразуем строку datetime-local в объект Date
        const dateTo = new Date(filters.dateTo);
        // Проверяем, что дата валидна
        if (!isNaN(dateTo.getTime())) {
            queryParams.append('transaction_date-lw', formatDateToSQL(dateTo));
        }
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
    if (filters.account) queryParams.append('account_number', filters.account);
    if (filters.recipient) queryParams.append('recipient_number', filters.recipient);
    if (filters.tin) queryParams.append('recipient_tin-bnum', filters.tin);
    if (filters.phone) {
        if (PhoneNumberUtils.isValid(filters.phone)) {
            queryParams.append('recipient_phone', PhoneNumberUtils.clean(filters.phone));
        }
    }
    if (filters.legalType) {
        const legalTypeId = legalTypeSelect.value;
        queryParams.append('legal_type_id-num', legalTypeId);
    }

    return queryParams.toString();
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
        PhoneNumberUtils.setupValidation(input);
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

    // Destroy existing charts if they exist
    Object.values(charts).forEach(chart => {
        if (chart) {
            chart.destroy();
        }
    });
    charts = {}; // Reset charts object

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
    if (!data) return;

    // Update transactions by period chart
    if (charts.transactionsByPeriod && data.transactionsByPeriod && data.transactionsByPeriod[currentPeriod]) {
        updateChartData(charts.transactionsByPeriod, data.transactionsByPeriod[currentPeriod]);
    }

    // Update debit transactions chart
    if (charts.debitTransactions && data.transactionsByType && data.transactionsByType.debit && data.transactionsByType.debit[currentPeriod]) {
        updateChartData(charts.debitTransactions, data.transactionsByType.debit[currentPeriod]);
    }

    // Update credit transactions chart
    if (charts.creditTransactions && data.transactionsByType && data.transactionsByType.credit && data.transactionsByType.credit[currentPeriod]) {
        updateChartData(charts.creditTransactions, data.transactionsByType.credit[currentPeriod]);
    }

    // Update income vs expense chart
    if (charts.incomeExpenseComparison && data.incomeVsExpense && data.incomeVsExpense[currentPeriod]) {
        updateIncomeExpenseChart(charts.incomeExpenseComparison, data.incomeVsExpense[currentPeriod]);
    }
}

// Helper function to update chart data
function updateChartData(chart, newData) {
    if (!chart || !newData || !newData.labels || !newData.values) {
        console.warn('Invalid data or chart for update:', { chart, newData });
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
    if (!chart || !newData || !newData.labels || !newData.income || !newData.expense) {
        console.warn('Invalid data or chart for income/expense update:', { chart, newData });
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
                label: 'Списания',
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

// Функция для подтверждения транзакции
function confirmTransaction(transactionId) {
    console.log('Confirming transaction with ID:', transactionId);
    const token = localStorage.getItem('authToken');
    
    fetch(`http://localhost:7070/api/confirm_transaction/${transactionId}`, {
        method: 'PUT',
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            console.log('Transaction confirmed successfully:', transactionId);
            // Обновляем список транзакций
            getUserTransactions();
        } else {
            console.error('Error confirming transaction:', data.error);
        }
    })
    .catch(error => {
        console.error('Error during transaction confirmation:', error);
    });
}

// Функция для удаления транзакции
function deleteTransaction(transactionId) {
    console.log('Deleting transaction with ID:', transactionId);
    const token = localStorage.getItem('authToken');
    
    fetch(`http://localhost:7070/api/delete_transaction/${transactionId}`, {
        method: 'PUT',
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            console.log('Transaction deleted successfully:', transactionId);
            // Обновляем список транзакций
            getUserTransactions();
        } else {
            console.error('Error deleting transaction:', data.error);
        }
    })
    .catch(error => {
        console.error('Error during transaction deletion:', error);
    });
}