// Configuration
const API_BASE_URL = 'http://localhost:8081/api';
let alertsData = [];
let logsData = [];

// DOM Elements
const sidebar = document.getElementById('sidebar');
const sidebarToggle = document.getElementById('sidebarToggle');
const menuToggle = document.getElementById('menuToggle');
const mainContent = document.getElementById('mainContent');

document.addEventListener('DOMContentLoaded', function() {
    setupEventListeners();
    refreshData();
    setInterval(refreshData, 30000);
});

function setupEventListeners() {
    // Sidebar toggle
    sidebarToggle.addEventListener('click', toggleSidebar);
    menuToggle.addEventListener('click', toggleSidebarMobile);

    document.querySelectorAll('.stat-card').forEach(card => {
        card.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-5px)';
            this.style.boxShadow = '0 10px 20px rgba(0, 0, 0, 0.1), 0 6px 6px rgba(0, 0, 0, 0.1)';
        });

        card.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0)';
            this.style.boxShadow = '0 1px 3px rgba(0, 0, 0, 0.12), 0 1px 2px rgba(0, 0, 0, 0.24)';
        });
    });
}

function toggleSidebar() {
    sidebar.classList.toggle('collapsed');
    mainContent.classList.toggle('sidebar-collapsed');
}

function toggleSidebarMobile() {
    sidebar.classList.toggle('visible');
}

async function refreshData() {
    try {
        await Promise.all([
            fetchAlerts(),
            fetchLogs(),
            fetchDashboardStats()
        ]);
        updateConnectionStatus(true);
    } catch (error) {
        console.error('Error refreshing data:', error);
        updateConnectionStatus(false);
    }
}

async function fetchAlerts() {
    try {
        const response = await fetch(`${API_BASE_URL}/alerts/recent`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const apiResponse = await response.json();
        console.log('Alerts API Response:', apiResponse);

        // Handle ApiResponse wrapper
        if (apiResponse.status === 'SUCCESS' && apiResponse.data) {
            alertsData = Array.isArray(apiResponse.data) ? apiResponse.data : [apiResponse.data];
        } else {
            alertsData = [];
        }

        renderAlerts();
    } catch (error) {
        console.error('Error fetching alerts:', error);
        alertsData = [];
        document.getElementById('alertsPanel').innerHTML =
            '<div class="error-message"><i class="fas fa-exclamation-circle"></i> Failed to load alerts: ' + error.message + '</div>';
    }
}

async function fetchLogs() {
    try {
        const response = await fetch(`${API_BASE_URL}/logs/recent`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const apiResponse = await response.json();
        console.log('Logs API Response:', apiResponse);

        // Handle ApiResponse wrapper
        if (apiResponse.status === 'SUCCESS' && apiResponse.data) {
            logsData = Array.isArray(apiResponse.data) ? apiResponse.data : [apiResponse.data];
        } else {
            logsData = [];
        }

        renderLogs();
    } catch (error) {
        console.error('Error fetching logs:', error);
        logsData = [];
        document.getElementById('logsPanel').innerHTML =
            '<div class="error-message"><i class="fas fa-exclamation-circle"></i> Failed to load logs: ' + error.message + '</div>';
    }
}

async function fetchDashboardStats() {
    try {
        const response = await fetch(`${API_BASE_URL}/alerts/dashboard`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const apiResponse = await response.json();
        console.log('Dashboard API Response:', apiResponse);

        // Handle ApiResponse wrapper
        if (apiResponse.status === 'SUCCESS' && apiResponse.data) {
            updateDashboardStats(apiResponse.data);
        }
    } catch (error) {
        console.error('Error fetching dashboard stats:', error);
    }
}

// Render alerts in the panel
function renderAlerts() {
    const alertsPanel = document.getElementById('alertsPanel');

    if (alertsData.length === 0) {
        alertsPanel.innerHTML = '<div class="empty-state"><i class="fas fa-check-circle"></i> No recent alerts</div>';
        return;
    }

    const alertsHTML = alertsData.map(alert => `
        <div class="alert-item ${alert.severity.toLowerCase()}">
            <div class="alert-header">
                <span class="alert-type">${alert.alertType.replace('_', ' ')}</span>
                <span class="alert-time">${formatTime(alert.timestamp)}</span>
            </div>
            <div class="alert-description">${alert.description}</div>
            ${alert.sourceIp ? `<div class="alert-ip"><i class="fas fa-network-wired"></i> ${alert.sourceIp}</div>` : ''}
            <div class="alert-footer">
                <span class="severity-badge ${alert.severity.toLowerCase()}">
                    ${alert.severity}
                </span>
            </div>
        </div>
    `).join('');

    alertsPanel.innerHTML = alertsHTML;
}

function renderLogs() {
    const logsPanel = document.getElementById('logsPanel');

    if (logsData.length === 0) {
        logsPanel.innerHTML = '<div class="empty-state"><i class="fas fa-file-alt"></i> No recent logs</div>';
        return;
    }

    const logsHTML = logsData.slice(0, 20).map(log => {
        let logClass = 'log-item';
        if (log.statusCode && (log.statusCode.startsWith('4') || log.statusCode.startsWith('5'))) {
            logClass += ' log-error';
        } else if (log.logLevel === 'WARN') {
            logClass += ' log-warning';
        }

        return `
            <div class="${logClass}">
                <div class="log-header">
                    <span class="log-source"><i class="fas fa-server"></i> ${log.source}</span>
                    <span class="log-time">${formatTime(log.timestamp)}</span>
                </div>
                <div class="log-message">${log.message}</div>
                <div class="log-meta">
                    ${log.ipAddress ? `<span><i class="fas fa-network-wired"></i> ${log.ipAddress}</span>` : ''}
                    ${log.statusCode ? `<span><i class="fas fa-code"></i> ${log.statusCode}</span>` : ''}
                    <span><i class="fas fa-layer-group"></i> ${log.logLevel || 'INFO'}</span>
                </div>
            </div>
        `;
    }).join('');

    logsPanel.innerHTML = logsHTML;
}

// Update dashboard statistics
function updateDashboardStats(stats) {
    console.log('Updating dashboard stats:', stats);

    document.getElementById('activeAlerts').textContent = stats.totalUnresolvedAlerts || 0;
    document.getElementById('logsProcessed').textContent = logsData.length || 0;

    const criticalCount = alertsData.filter(alert => alert.severity === 'CRITICAL').length;
    document.getElementById('criticalAlerts').textContent = criticalCount;

    const uniqueIPs = new Set(logsData.map(log => log.ipAddress).filter(ip => ip));
    document.getElementById('uniqueIPs').textContent = uniqueIPs.size;
}

// Update connection status
function updateConnectionStatus(isOnline) {
    const statusElement = document.getElementById('connectionStatus');
    const indicator = document.querySelector('.connection-status .status-indicator');

    if (isOnline) {
        statusElement.textContent = 'System Online';
        indicator.className = 'status-indicator status-online';
    } else {
        statusElement.textContent = 'Connection Error';
        indicator.className = 'status-indicator status-offline';
    }
}

function formatTime(timestamp) {
    const date = new Date(timestamp);
    return date.toLocaleTimeString() + ' ' + date.toLocaleDateString();
}

// Simulate attack for demo purposes
async function simulateAttack() {
    const attackTypes = [
        {
            source: 'web-server',
            message: 'Failed login attempt for user admin',
            ipAddress: '192.168.1.100',
            statusCode: '401',
            logLevel: 'WARN'
        },
        {
            source: 'firewall',
            message: 'Blocked connection from suspicious IP',
            ipAddress: '10.0.0.50',
            statusCode: '403',
            logLevel: 'ERROR'
        },
        {
            source: 'database',
            message: 'SQL injection attempt detected',
            ipAddress: '172.16.0.25',
            statusCode: '500',
            logLevel: 'CRITICAL'
        }
    ];

    try {
        for (const attack of attackTypes) {
            await fetch(`${API_BASE_URL}/logs/ingest`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(attack)
            });
        }

        setTimeout(refreshData, 1000);
        showNotification('🚨 Attack simulation completed! Check the alerts panel.', 'success');
    } catch (error) {
        console.error('Error simulating attack:', error);
        showNotification('❌ Failed to simulate attack. Make sure the backend is running.', 'danger');
    }
}

// Generate test logs
async function generateTestLogs() {
    const testLogs = [
        {
            source: 'nginx',
            message: 'GET /api/users HTTP/1.1 200',
            ipAddress: '192.168.1.15',
            statusCode: '200',
            logLevel: 'INFO'
        },
        {
            source: 'application',
            message: 'User login successful: john.doe@company.com',
            ipAddress: '10.0.0.5',
            statusCode: '200',
            logLevel: 'INFO'
        },
        {
            source: 'system',
            message: 'Disk usage at 85%',
            ipAddress: null,
            statusCode: null,
            logLevel: 'WARN'
        }
    ];

    try {
        for (const log of testLogs) {
            await fetch(`${API_BASE_URL}/logs/ingest`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(log)
            });
        }

        setTimeout(refreshData, 1000);
        showNotification('📝 Test logs generated successfully!', 'success');
    } catch (error) {
        console.error('Error generating test logs:', error);
        showNotification('❌ Failed to generate test logs. Make sure the backend is running.', 'danger');
    }
}

// Show notification
function showNotification(message, type) {
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.innerHTML = `
        <div class="notification-content">
            <i class="fas ${type === 'success' ? 'fa-check-circle' : 'fa-exclamation-circle'}"></i>
            <span>${message}</span>
        </div>
        <button class="notification-close"><i class="fas fa-times"></i></button>
    `;

    document.body.appendChild(notification);

    // Auto-remove after 5 seconds
    setTimeout(() => {
        notification.classList.add('fade-out');
        setTimeout(() => notification.remove(), 300);
    }, 5000);

    // Close button
    notification.querySelector('.notification-close').addEventListener('click', () => {
        notification.remove();
    });
}