// Dashboard functionality
document.addEventListener('DOMContentLoaded', initDashboard);

// Main initialization function
function initDashboard() {
    // Only initialize if the dashboard container exists
    const dashboardContainer = document.getElementById('dashboard-container');
    if (!dashboardContainer) return;

    // Load Chart.js from CDN
    loadScript('https://cdn.jsdelivr.net/npm/chart.js', () => {
        // Generate mock data
        const data = generateMockData();
        
        // Initialize all charts
        createTransactionsByPeriodChart(data);
        createDebitTransactionsChart(data);
        createCreditTransactionsChart(data);
        createIncomeExpenseComparisonChart(data);
        createTransactionStatusChart(data);
        createBankStatisticsChart(data);
        createExpenseCategoriesChart(data);
        createIncomeCategoriesChart(data);
    });
}

// Helper function to load external scripts
function loadScript(url, callback) {
    const script = document.createElement('script');
    script.src = url;
    script.onload = callback;
    document.head.appendChild(script);
}

// Function to generate mock data for charts
function generateMockData() {
    return {
        transactionsByPeriod: {
            weekly: [120, 150, 180, 190, 210, 250, 230, 260, 240, 220, 200, 180],
            monthly: [800, 950, 1100, 1050, 1200, 1300, 1250, 1400, 1350, 1300, 1200, 1100],
            quarterly: [2800, 3200, 3600, 3400],
            yearly: [12000, 13500, 14800, 16000, 15500]
        },
        transactionsByType: {
            debit: {
                labels: ['Янв', 'Фев', 'Мар', 'Апр', 'Май', 'Июн', 'Июл', 'Авг', 'Сен', 'Окт', 'Ноя', 'Дек'],
                values: [500, 650, 600, 700, 750, 800, 850, 900, 850, 800, 750, 700]
            },
            credit: {
                labels: ['Янв', 'Фев', 'Мар', 'Апр', 'Май', 'Июн', 'Июл', 'Авг', 'Сен', 'Окт', 'Ноя', 'Дек'],
                values: [700, 800, 750, 850, 900, 950, 1000, 1050, 1000, 950, 900, 850]
            }
        },
        incomeVsExpense: {
            income: [700, 800, 750, 850, 900, 950, 1000, 1050, 1000, 950, 900, 850],
            expense: [500, 650, 600, 700, 750, 800, 850, 900, 850, 800, 750, 700]
        },
        transactionStatus: {
            completed: 8500,
            cancelled: 420
        },
        bankStatistics: {
            sender: {
                'Сбербанк': 3200,
                'Тинькофф': 2800,
                'ВТБ': 1900,
                'Альфа-Банк': 1600,
                'Райффайзен': 1200,
                'Другие': 800
            },
            receiver: {
                'Сбербанк': 2900,
                'Тинькофф': 2500,
                'ВТБ': 2100,
                'Альфа-Банк': 1800,
                'Райффайзен': 1300,
                'Другие': 900
            }
        },
        categories: {
            expense: {
                'Продукты': 2500,
                'Транспорт': 1200,
                'Развлечения': 1800,
                'Жилье': 3200,
                'Связь': 800,
                'Одежда': 1500,
                'Другое': 1000
            },
            income: {
                'Зарплата': 8500,
                'Фриланс': 2200,
                'Инвестиции': 1800,
                'Подарки': 600,
                'Другое': 900
            }
        }
    };
}

// Create chart for transactions by period (week/month/quarter/year)
function createTransactionsByPeriodChart(data) {
    const ctx = document.getElementById('transactions-by-period-chart').getContext('2d');
    
    const chart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: ['Янв', 'Фев', 'Мар', 'Апр', 'Май', 'Июн', 'Июл', 'Авг', 'Сен', 'Окт', 'Ноя', 'Дек'],
            datasets: [{
                label: 'Еженедельно',
                data: data.transactionsByPeriod.weekly,
                borderColor: 'rgba(75, 192, 192, 1)',
                backgroundColor: 'rgba(75, 192, 192, 0.2)',
                borderWidth: 2,
                hidden: true
            }, {
                label: 'Ежемесячно',
                data: data.transactionsByPeriod.monthly,
                borderColor: 'rgba(54, 162, 235, 1)',
                backgroundColor: 'rgba(54, 162, 235, 0.2)',
                borderWidth: 2
            }, {
                label: 'Ежеквартально',
                data: [null, null, data.transactionsByPeriod.quarterly[0], null, null, 
                       data.transactionsByPeriod.quarterly[1], null, null, 
                       data.transactionsByPeriod.quarterly[2], null, null, 
                       data.transactionsByPeriod.quarterly[3]],
                borderColor: 'rgba(153, 102, 255, 1)',
                backgroundColor: 'rgba(153, 102, 255, 0.2)',
                borderWidth: 2,
                hidden: true
            }, {
                label: 'Ежегодно',
                data: [data.transactionsByPeriod.yearly[0], null, null, null, 
                       data.transactionsByPeriod.yearly[1], null, null, null, 
                       data.transactionsByPeriod.yearly[2], null, null, null],
                borderColor: 'rgba(255, 159, 64, 1)',
                backgroundColor: 'rgba(255, 159, 64, 0.2)',
                borderWidth: 2,
                hidden: true
            }]
        },
        options: {
            responsive: true,
            plugins: {
                title: {
                    display: true,
                    text: 'Динамика по количеству транзакций',
                    font: {
                        size: 16
                    }
                },
                legend: {
                    position: 'top',
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    title: {
                        display: true,
                        text: 'Количество транзакций'
                    }
                },
                x: {
                    title: {
                        display: true,
                        text: 'Период'
                    }
                }
            }
        }
    });

    // Add period selector
    const periodSelector = document.getElementById('period-selector');
    if (periodSelector) {
        periodSelector.addEventListener('change', function() {
            const period = this.value;
            chart.data.datasets.forEach((dataset, index) => {
                dataset.hidden = period !== index.toString();
            });
            chart.update();
        });
    }
}

// Create chart for debit transactions 
function createDebitTransactionsChart(data) {
    const ctx = document.getElementById('debit-transactions-chart').getContext('2d');
    
    new Chart(ctx, {
        type: 'bar',
        data: {
            labels: data.transactionsByType.debit.labels,
            datasets: [{
                label: 'Дебетовые транзакции',
                data: data.transactionsByType.debit.values,
                backgroundColor: 'rgba(255, 99, 132, 0.7)',
                borderColor: 'rgba(255, 99, 132, 1)',
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            plugins: {
                title: {
                    display: true,
                    text: 'Динамика по дебетовым транзакциям',
                    font: {
                        size: 16
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    title: {
                        display: true,
                        text: 'Количество транзакций'
                    }
                },
                x: {
                    title: {
                        display: true,
                        text: 'Месяц'
                    }
                }
            }
        }
    });
}

// Create chart for credit transactions
function createCreditTransactionsChart(data) {
    const ctx = document.getElementById('credit-transactions-chart').getContext('2d');
    
    new Chart(ctx, {
        type: 'bar',
        data: {
            labels: data.transactionsByType.credit.labels,
            datasets: [{
                label: 'Кредитовые транзакции',
                data: data.transactionsByType.credit.values,
                backgroundColor: 'rgba(54, 162, 235, 0.7)',
                borderColor: 'rgba(54, 162, 235, 1)',
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            plugins: {
                title: {
                    display: true,
                    text: 'Динамика по кредитовым транзакциям',
                    font: {
                        size: 16
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    title: {
                        display: true,
                        text: 'Количество транзакций'
                    }
                },
                x: {
                    title: {
                        display: true,
                        text: 'Месяц'
                    }
                }
            }
        }
    });
}

// Create chart comparing income vs expense
function createIncomeExpenseComparisonChart(data) {
    const ctx = document.getElementById('income-expense-comparison-chart').getContext('2d');
    
    new Chart(ctx, {
        type: 'line',
        data: {
            labels: ['Янв', 'Фев', 'Мар', 'Апр', 'Май', 'Июн', 'Июл', 'Авг', 'Сен', 'Окт', 'Ноя', 'Дек'],
            datasets: [{
                label: 'Поступления',
                data: data.incomeVsExpense.income,
                borderColor: 'rgba(75, 192, 192, 1)',
                backgroundColor: 'rgba(75, 192, 192, 0.2)',
                fill: true,
                tension: 0.4
            }, {
                label: 'Расходы',
                data: data.incomeVsExpense.expense,
                borderColor: 'rgba(255, 99, 132, 1)',
                backgroundColor: 'rgba(255, 99, 132, 0.2)',
                fill: true,
                tension: 0.4
            }]
        },
        options: {
            responsive: true,
            plugins: {
                title: {
                    display: true,
                    text: 'Сравнение поступлений и расходов',
                    font: {
                        size: 16
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    title: {
                        display: true,
                        text: 'Сумма (₽)'
                    }
                },
                x: {
                    title: {
                        display: true,
                        text: 'Месяц'
                    }
                }
            }
        }
    });
}

// Create chart for transaction status (completed vs cancelled)
function createTransactionStatusChart(data) {
    const ctx = document.getElementById('transaction-status-chart').getContext('2d');
    
    new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: ['Проведенные', 'Отмененные'],
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
            plugins: {
                title: {
                    display: true,
                    text: 'Статус транзакций',
                    font: {
                        size: 16
                    }
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            const label = context.label || '';
                            const value = context.raw || 0;
                            const total = context.dataset.data.reduce((a, b) => a + b, 0);
                            const percentage = Math.round((value / total) * 100);
                            return `${label}: ${value} (${percentage}%)`;
                        }
                    }
                }
            }
        }
    });
}

// Create chart for bank statistics
function createBankStatisticsChart(data) {
    const ctx = document.getElementById('bank-statistics-chart').getContext('2d');
    
    const banks = Object.keys(data.bankStatistics.sender);
    const senderValues = Object.values(data.bankStatistics.sender);
    const receiverValues = Object.values(data.bankStatistics.receiver);
    
    new Chart(ctx, {
        type: 'bar',
        data: {
            labels: banks,
            datasets: [{
                label: 'Банк отправителя',
                data: senderValues,
                backgroundColor: 'rgba(153, 102, 255, 0.7)',
                borderColor: 'rgba(153, 102, 255, 1)',
                borderWidth: 1
            }, {
                label: 'Банк получателя',
                data: receiverValues,
                backgroundColor: 'rgba(255, 159, 64, 0.7)',
                borderColor: 'rgba(255, 159, 64, 1)',
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            plugins: {
                title: {
                    display: true,
                    text: 'Статистика по банкам',
                    font: {
                        size: 16
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    title: {
                        display: true,
                        text: 'Количество транзакций'
                    }
                },
                x: {
                    title: {
                        display: true,
                        text: 'Банк'
                    }
                }
            }
        }
    });
}

// Create chart for expense categories
function createExpenseCategoriesChart(data) {
    const ctx = document.getElementById('expense-categories-chart').getContext('2d');
    
    const categories = Object.keys(data.categories.expense);
    const values = Object.values(data.categories.expense);
    
    new Chart(ctx, {
        type: 'pie',
        data: {
            labels: categories,
            datasets: [{
                data: values,
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
            plugins: {
                title: {
                    display: true,
                    text: 'Категории расходов',
                    font: {
                        size: 16
                    }
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            const label = context.label || '';
                            const value = context.raw || 0;
                            const total = context.dataset.data.reduce((a, b) => a + b, 0);
                            const percentage = Math.round((value / total) * 100);
                            return `${label}: ${value}₽ (${percentage}%)`;
                        }
                    }
                }
            }
        }
    });
}

// Create chart for income categories
function createIncomeCategoriesChart(data) {
    const ctx = document.getElementById('income-categories-chart').getContext('2d');
    
    const categories = Object.keys(data.categories.income);
    const values = Object.values(data.categories.income);
    
    new Chart(ctx, {
        type: 'pie',
        data: {
            labels: categories,
            datasets: [{
                data: values,
                backgroundColor: [
                    'rgba(75, 192, 192, 0.7)',
                    'rgba(54, 162, 235, 0.7)',
                    'rgba(153, 102, 255, 0.7)',
                    'rgba(255, 159, 64, 0.7)',
                    'rgba(199, 199, 199, 0.7)'
                ],
                borderColor: [
                    'rgba(75, 192, 192, 1)',
                    'rgba(54, 162, 235, 1)',
                    'rgba(153, 102, 255, 1)',
                    'rgba(255, 159, 64, 1)',
                    'rgba(199, 199, 199, 1)'
                ],
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            plugins: {
                title: {
                    display: true,
                    text: 'Категории поступлений',
                    font: {
                        size: 16
                    }
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            const label = context.label || '';
                            const value = context.raw || 0;
                            const total = context.dataset.data.reduce((a, b) => a + b, 0);
                            const percentage = Math.round((value / total) * 100);
                            return `${label}: ${value}₽ (${percentage}%)`;
                        }
                    }
                }
            }
        }
    });
} 