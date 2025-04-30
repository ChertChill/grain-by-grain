// Dashboard functionality
document.addEventListener('DOMContentLoaded', initDashboard);

// Store chart instances
let charts = {};
let currentPeriod = 'monthly'; // Default period

// Main initialization function
function initDashboard() {
    // Only initialize if the dashboard container exists
    const dashboardContainer = document.getElementById('dashboard-container');
    if (!dashboardContainer) return;

    // Load Chart.js from CDN
    loadScript('https://cdn.jsdelivr.net/npm/chart.js', () => {
        // Generate mock data
        const data = generateMockData();
        
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

// Function to generate mock data for charts
function generateMockData() {
    return {
        transactionsByPeriod: {
            weekly: {
                labels: ['Неделя 1', 'Неделя 2', 'Неделя 3', 'Неделя 4'],
                values: [120, 150, 180, 190]
            },
            monthly: {
                labels: ['Янв', 'Фев', 'Мар', 'Апр', 'Май', 'Июн', 'Июл', 'Авг', 'Сен', 'Окт', 'Ноя', 'Дек'],
                values: [800, 950, 1100, 1050, 1200, 1300, 1250, 1400, 1350, 1300, 1200, 1100]
            },
            quarterly: {
                labels: ['Q1', 'Q2', 'Q3', 'Q4'],
                values: [2800, 3200, 3600, 3400]
            },
            yearly: {
                labels: ['2020', '2021', '2022', '2023', '2024'],
                values: [12000, 13500, 14800, 16000, 15500]
            }
        },
        transactionsByType: {
            debit: {
                weekly: {
                    labels: ['Неделя 1', 'Неделя 2', 'Неделя 3', 'Неделя 4'],
                    values: [150, 180, 160, 190]
                },
                monthly: {
                    labels: ['Янв', 'Фев', 'Мар', 'Апр', 'Май', 'Июн', 'Июл', 'Авг', 'Сен', 'Окт', 'Ноя', 'Дек'],
                    values: [500, 650, 600, 700, 750, 800, 850, 900, 850, 800, 750, 700]
                },
                quarterly: {
                    labels: ['Q1', 'Q2', 'Q3', 'Q4'],
                    values: [1800, 2200, 2400, 2100]
                },
                yearly: {
                    labels: ['2020', '2021', '2022', '2023', '2024'],
                    values: [8500, 9200, 9800, 10500, 11000]
                }
            },
            credit: {
                weekly: {
                    labels: ['Неделя 1', 'Неделя 2', 'Неделя 3', 'Неделя 4'],
                    values: [200, 220, 210, 230]
                },
                monthly: {
                    labels: ['Янв', 'Фев', 'Мар', 'Апр', 'Май', 'Июн', 'Июл', 'Авг', 'Сен', 'Окт', 'Ноя', 'Дек'],
                    values: [700, 800, 750, 850, 900, 950, 1000, 1050, 1000, 950, 900, 850]
                },
                quarterly: {
                    labels: ['Q1', 'Q2', 'Q3', 'Q4'],
                    values: [2500, 2800, 3000, 2700]
                },
                yearly: {
                    labels: ['2020', '2021', '2022', '2023', '2024'],
                    values: [10500, 11500, 12500, 13500, 14000]
                }
            }
        },
        incomeVsExpense: {
            weekly: {
                labels: ['Неделя 1', 'Неделя 2', 'Неделя 3', 'Неделя 4'],
                income: [250, 280, 270, 290],
                expense: [180, 200, 190, 210]
            },
            monthly: {
                labels: ['Янв', 'Фев', 'Мар', 'Апр', 'Май', 'Июн', 'Июл', 'Авг', 'Сен', 'Окт', 'Ноя', 'Дек'],
                income: [700, 800, 750, 850, 900, 950, 1000, 1050, 1000, 950, 900, 850],
                expense: [500, 650, 600, 700, 750, 800, 850, 900, 850, 800, 750, 700]
            },
            quarterly: {
                labels: ['Q1', 'Q2', 'Q3', 'Q4'],
                income: [2500, 2800, 3000, 2700],
                expense: [1800, 2200, 2400, 2100]
            },
            yearly: {
                labels: ['2020', '2021', '2022', '2023', '2024'],
                income: [10500, 11500, 12500, 13500, 14000],
                expense: [8500, 9200, 9800, 10500, 11000]
            }
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