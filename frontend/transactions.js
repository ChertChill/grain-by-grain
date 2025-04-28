// !!! Прописать как отдельную функцию для отображения транзакций в логине, регистрации и проверке юзера 

// Функция для получения транзакций пользователя

function getUserTransactions() {
    const transactionsError = document.getElementById('transactions-error');
    transactionsError.textContent = '';

    const token = localStorage.getItem('authToken');

    // API запрос для входа
    fetch('http://localhost:7070/api/get_user_transactions', {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
        .then(response => response.json())
        .then(data => {
            console.log('Response data:', data);
            if (Array.isArray(data)) {
                if (data.length === 0) {
                    transactionsError.textContent = 'Транзакции не найдены.';
                } else {
                    // Получение и отображение транзакций
                    console.log(data);

                    /* -- [ ] -- */
                }
            } else {
                transactionsError.textContent = 'Неверный формат данных от сервера';
            }
        })
        .catch(error => {
            console.error('Ошибка:', error);
            transactionsError.textContent = error.message || 'Ошибка при загрузке транзакций';
        });
}



// Получение транзакций по кнопке "Сброс параметров"

const resetButton = document.getElementById('reset-button');

resetButton.addEventListener('click', function (e) {
    console.log('Reset button clicked');
    e.preventDefault();
    getUserTransactions();
});