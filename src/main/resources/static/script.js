// Configuration
const API_BASE_URL = 'http://localhost:8081/api';
let alertsData = [];
let logsData = [];
let isOnline = false;

// DOM Elements
const sidebar = document.getElementById('sidebar');
const sidebarToggle = document.getElementById('sidebarToggle');
const menuToggle = document.getElementById('menuToggle');
const mainContent = document.getElementById('mainContent');

document.addEventListener('DOMContentLoaded', function() {
    console.log('Dashboard initializing...');
    setupEventListeners();
    refreshData();
    // Refresh every 30 seconds
    setInterval(refreshData, 30000);
});

function setupEventListeners() {
    console.log('Setting up event listeners...');

    // Sidebar toggle
    if (sidebarToggle) {
        sidebarToggle.addEventListener('click', toggleSidebar);
    }

    if (menuToggle) {
        menuToggle.addEventListener('click', toggleSidebarMobile);
    }

    // Stat cards hover effects
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

    // Add click handlers for buttons
    const refreshBtn = document.querySelector('button[onclick="refreshData()"]');
    if (refreshBtn) {
        refreshBtn.removeAttribute('onclick');
        refreshBtn.addEventListener('click', refreshData);
    }

    const simulateBtn = document.querySelector('button[onclick="simulateAttack()"]');
    if (simulateBtn) {
        simulateBtn.removeAttribute('onclick');
        simulateBtn.addEventListener('click', simulateAttack);
    }

    const testLogsBtn = document.querySelector('button[onclick="generateTestLogs()"]');
    if (testLogsBtn) {
        testLogsBtn.removeAttribute('onclick');
        testLogsBtn.addEventListener('click', generateTestLogs);
    }
}

function toggleSidebar() {
    console.log('Toggling sidebar...');
    if (sidebar) {
        sidebar.classList.toggle('collapsed');
    }
    if (mainContent) {
        mainContent.classList.toggle('sidebar-collapsed');
    }
}

function toggleSidebarMobile() {
    console.log('Toggling mobile sidebar...');
    if (sidebar) {
        sidebar.classList.toggle('visible');
    }
}

async function refreshData() {
    console.log('Refreshing data...');

    try {
        const results = await Promise.allSettled([
            fetchAlerts(),
            fetchLogs(),
            fetchDashboardStats()
        ]);

        // Check if at least one request succeeded
        const hasSuccess = results.some(result => result.status === 'fulfilled');
        updateConnectionStatus(hasSuccess);

        if (!hasSuccess) {
            console.error('All API requests failed');
            showNotification('❌ Failed to connect to backend. Check if the server is running on port 8081.', 'danger');
        }
    } catch (error) {
        console.error('Error refreshing data:', error);
        updateConnectionStatus(false);
        showNotification('❌ Connection error: ' + error.message, 'danger');
    }
}

async function fetchAlerts() {
    console.log('Fetching alerts...');

    try {
        const response = await fetch(`${API_BASE_URL}/alerts/recent`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            // Add timeout
            signal: AbortSignal.timeout(10000)
        });

        console.log('Alerts response status:', response.status);

        if (!response.ok) {
            if (response.status === 404) {
                console.log('No alerts found (404)');
                alertsData = [];
                renderAlerts();
                return;
            }
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }

        const apiResponse = await response.json();
        console.log('Alerts API Response:', apiResponse);

        // Handle your backend's ApiResponse wrapper
        if (apiResponse.status === 'SUCCESS' && apiResponse.data) {
            alertsData = Array.isArray(apiResponse.data) ? apiResponse.data : [apiResponse.data];
        } else if (apiResponse.status === 'ERROR' && apiResponse.message === 'No recent alerts found') {
            alertsData = [];
        } else {
            alertsData = [];
        }

        renderAlerts();
        return true;
    } catch (error) {
        console.error('Error fetching alerts:', error);
        alertsData = [];
        const alertsPanel = document.getElementById('alertsPanel');
        if (alertsPanel) {
            alertsPanel.innerHTML =
                `<div class="error-message">
                    <i class="fas fa-exclamation-circle"></i>
                    Failed to load alerts: ${error.message}
                </div>`;
        }
        throw error;
    }
}

async function fetchLogs() {
    console.log('Fetching logs...');

    try {
        const response = await fetch(`${API_BASE_URL}/logs/recent`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            signal: AbortSignal.timeout(10000)
        });

        console.log('Logs response status:', response.status);

        if (!response.ok) {
            if (response.status === 404) {
                console.log('No logs found (404)');
                logsData = [];
                renderLogs();
                return;
            }
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }

        const apiResponse = await response.json();
        console.log('Logs API Response:', apiResponse);

        // Handle your backend's ApiResponse wrapper
        if (apiResponse.status === 'SUCCESS' && apiResponse.data) {
            logsData = Array.isArray(apiResponse.data) ? apiResponse.data : [apiResponse.data];
        } else if (apiResponse.status === 'ERROR' && apiResponse.message === 'No recent logs found') {
            logsData = [];
        } else {
            logsData = [];
        }

        renderLogs();
        return true;
    } catch (error) {
        console.error('Error fetching logs:', error);
        logsData = [];
        const logsPanel = document.getElementById('logsPanel');
        if (logsPanel) {
            logsPanel.innerHTML =
                `<div class="error-message">
                    <i class="fas fa-exclamation-circle"></i>
                    Failed to load logs: ${error.message}
                </div>`;
        }
        throw error;
    }
}

async function fetchDashboardStats() {
    console.log('Fetching dashboard stats...');

    try {
        const response = await fetch(`${API_BASE_URL}/alerts/dashboard`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            signal: AbortSignal.timeout(10000)
        });

        console.log('Dashboard response status:', response.status);

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }

        const apiResponse = await response.json();
        console.log('Dashboard API Response:', apiResponse);

        // Handle your backend's ApiResponse wrapper
        if (apiResponse.status === 'SUCCESS' && apiResponse.data) {
            updateDashboardStats(apiResponse.data);
        }
        return true;
    } catch (error) {
        console.error('Error fetching dashboard stats:', error);
        // Update with default values on error
        updateDashboardStats({});
        throw error;
    }
}

// Render alerts in the panel
function renderAlerts() {
    const alertsPanel = document.getElementById('alertsPanel');
    if (!alertsPanel) return;

    if (alertsData.length === 0) {
        alertsPanel.innerHTML = '<div class="empty-state"><i class="fas fa-check-circle"></i> No recent alerts</div>';
        return;
    }

    const alertsHTML = alertsData.map(alert => `
        <div class="alert-item ${(alert.severity || 'info').toLowerCase()}">
            <div class="alert-header">
                <span class="alert-type">${(alert.alertType || 'Unknown').replace('_', ' ')}</span>
                <span class="alert-time">${formatTime(alert.timestamp || alert.createdAt || new Date())}</span>
            </div>
            <div class="alert-description">${alert.description || alert.message || 'No description available'}</div>
            ${alert.sourceIp ? `<div class="alert-ip"><i class="fas fa-network-wired"></i> ${alert.sourceIp}</div>` : ''}
            <div class="alert-footer">
                <span class="severity-badge ${(alert.severity || 'info').toLowerCase()}">
                    ${alert.severity || 'INFO'}
                </span>
            </div>
        </div>
    `).join('');

    alertsPanel.innerHTML = alertsHTML;
}

function renderLogs() {
    const logsPanel = document.getElementById('logsPanel');
    if (!logsPanel) return;

    if (logsData.length === 0) {
        logsPanel.innerHTML = '<div class="empty-state"><i class="fas fa-file-alt"></i> No recent logs</div>';
        return;
    }

    const logsHTML = logsData.slice(0, 20).map(log => {
        let logClass = 'log-item';
        const statusCode = log.statusCode || '';
        const logLevel = log.logLevel || 'INFO';

        if (statusCode.startsWith('4') || statusCode.startsWith('5') || logLevel === 'ERROR') {
            logClass += ' log-error';
        } else if (logLevel === 'WARN') {
            logClass += ' log-warning';
        }

        return `
            <div class="${logClass}">
                <div class="log-header">
                    <span class="log-source"><i class="fas fa-server"></i> ${log.source || 'Unknown'}</span>
                    <span class="log-time">${formatTime(log.timestamp || log.createdAt || new Date())}</span>
                </div>
                <div class="log-message">${log.message || 'No message'}</div>
                <div class="log-meta">
                    ${log.ipAddress ? `<span><i class="fas fa-network-wired"></i> ${log.ipAddress}</span>` : ''}
                    ${statusCode ? `<span><i class="fas fa-code"></i> ${statusCode}</span>` : ''}
                    <span><i class="fas fa-layer-group"></i> ${logLevel}</span>
                </div>
            </div>
        `;
    }).join('');

    logsPanel.innerHTML = logsHTML;
}

// Update dashboard statistics
function updateDashboardStats(stats) {
    console.log('Updating dashboard stats:', stats);

    // Update stats with fallback values
    const activeAlertsEl = document.getElementById('activeAlerts');
    const logsProcessedEl = document.getElementById('logsProcessed');
    const criticalAlertsEl = document.getElementById('criticalAlerts');
    const uniqueIPsEl = document.getElementById('uniqueIPs');

    if (activeAlertsEl) {
        activeAlertsEl.textContent = stats.totalUnresolvedAlerts || alertsData.length || 0;
    }

    if (logsProcessedEl) {
        logsProcessedEl.textContent = stats.totalLogs || logsData.length || 0;
    }

    if (criticalAlertsEl) {
        const criticalCount = stats.criticalAlerts ||
            alertsData.filter(alert => (alert.severity || '').toUpperCase() === 'CRITICAL').length || 0;
        criticalAlertsEl.textContent = criticalCount;
    }

    if (uniqueIPsEl) {
        const uniqueIPs = new Set(
            logsData.map(log => log.ipAddress || log.sourceIp).filter(ip => ip)
        );
        uniqueIPsEl.textContent = stats.uniqueIPs || uniqueIPs.size || 0;
    }
}

// Update connection status
function updateConnectionStatus(online) {
    isOnline = online;
    const statusElement = document.getElementById('connectionStatus');
    const indicator = document.querySelector('.connection-status .status-indicator');

    if (statusElement) {
        statusElement.textContent = online ? 'System Online' : 'Connection Error';
    }

    if (indicator) {
        indicator.className = online ?
            'status-indicator status-online' :
            'status-indicator status-offline';
    }
}

function formatTime(timestamp) {
    try {
        const date = new Date(timestamp);
        if (isNaN(date.getTime())) {
            return 'Invalid Date';
        }
        return date.toLocaleTimeString() + ' ' + date.toLocaleDateString();
    } catch (error) {
        return 'Invalid Date';
    }
}

// Simulate attack for demo purposes
async function simulateAttack() {
    console.log('Simulating attack...');

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
        showNotification('🚨 Simulating attack...', 'info');

        for (const attack of attackTypes) {
            const response = await fetch(`${API_BASE_URL}/logs/ingest`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify(attack)
            });

            if (!response.ok) {
                throw new Error(`Failed to send attack log: ${response.status}`);
            }
        }

        // Wait a bit then refresh data
        setTimeout(() => {
            refreshData();
        }, 2000);

        showNotification('🚨 Attack simulation completed! Check the alerts panel.', 'success');
    } catch (error) {
        console.error('Error simulating attack:', error);
        showNotification('❌ Failed to simulate attack: ' + error.message, 'danger');
    }
}

// Generate test logs
async function generateTestLogs() {
    console.log('Generating test logs...');

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
        showNotification('📝 Generating test logs...', 'info');

        for (const log of testLogs) {
            const response = await fetch(`${API_BASE_URL}/logs/ingest`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify(log)
            });

            if (!response.ok) {
                throw new Error(`Failed to send test log: ${response.status}`);
            }
        }

        // Wait a bit then refresh data
        setTimeout(() => {
            refreshData();
        }, 2000);

        showNotification('📝 Test logs generated successfully!', 'success');
    } catch (error) {
        console.error('Error generating test logs:', error);
        showNotification('❌ Failed to generate test logs: ' + error.message, 'danger');
    }
}

// Show notification
function showNotification(message, type = 'info') {
    // Remove existing notifications
    const existingNotifications = document.querySelectorAll('.notification');
    existingNotifications.forEach(n => n.remove());

    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: ${type === 'success' ? '#48BB78' : type === 'danger' ? '#F56565' : '#4299E1'};
        color: white;
        padding: 15px 20px;
        border-radius: 8px;
        box-shadow: 0 4px 12px rgba(0,0,0,0.15);
        z-index: 10000;
        max-width: 400px;
        font-weight: 500;
        animation: slideIn 0.3s ease;
    `;

    notification.innerHTML = `
        <div style="display: flex; align-items: center; gap: 10px;">
            <i class="fas ${type === 'success' ? 'fa-check-circle' : type === 'danger' ? 'fa-exclamation-circle' : 'fa-info-circle'}"></i>
            <span>${message}</span>
            <button onclick="this.parentElement.parentElement.remove()" style="background: none; border: none; color: white; cursor: pointer; font-size: 16px; margin-left: 10px;">×</button>
        </div>
    `;

    // Add animation styles
    const style = document.createElement('style');
    style.textContent = `
        @keyframes slideIn {
            from { transform: translateX(100%); opacity: 0; }
            to { transform: translateX(0); opacity: 1; }
        }
    `;
    document.head.appendChild(style);

    document.body.appendChild(notification);

    // Auto-remove after 5 seconds
    setTimeout(() => {
        if (notification.parentNode) {
            notification.style.animation = 'slideIn 0.3s ease reverse';
            setTimeout(() => notification.remove(), 300);
        }
    }, 5000);
}

// Add some basic error handling for network issues
window.addEventListener('online', () => {
    console.log('Network connection restored');
    showNotification('🌐 Network connection restored', 'success');
    refreshData();
});

window.addEventListener('offline', () => {
    console.log('Network connection lost');
    showNotification('⚠️ Network connection lost', 'warning');
    updateConnectionStatus(false);
});

// Initialize on page load
console.log('Script loaded successfully');