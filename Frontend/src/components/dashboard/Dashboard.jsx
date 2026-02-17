

import React, { useState, useEffect } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import { PieChart, Pie, Cell, ResponsiveContainer, Legend, Tooltip } from 'recharts';

const API_BASE = 'http://localhost:8080/api';

const Dashboard = () => {
  const [stats, setStats] = useState(null);
  const { token, user } = useAuth();
  const isAdmin = user?.roles?.includes('ROLE_ADMIN');

  useEffect(() => {
    const fetchStats = async () => {
      try {
        if (isAdmin) {
          // Admin: Fetch global metrics
          const res = await fetch(`${API_BASE}/runs/metrics`, {
            headers: { 'Authorization': `Bearer ${token}` }
          });
          
          if (!res.ok) throw new Error('Failed to fetch metrics');
          const data = await res.json();
          
          // Transform admin data to match new format
          setStats({
            passed: data.passed || 0,
            failed: data.failed || 0,
            pending: 0,
            passRate: data.passRate || 0,
            suiteCount: 0,
            totalTestCases: data.total || 0
          });
        } else {
          // User: Fetch stats based on their test cases
          const res = await fetch(`${API_BASE}/users/me/stats`, {
            headers: { 'Authorization': `Bearer ${token}` }
          });
          
          if (!res.ok) throw new Error('Failed to fetch user stats');
          const data = await res.json();
          
          console.log('User stats loaded:', data);
          setStats(data);
        }
      } catch (err) {
        console.error('Stats error:', err);
        // Set default stats for graceful degradation
        setStats({
          passed: 0,
          failed: 0,
          pending: 0,
          passRate: 0,
          suiteCount: 0,
          totalTestCases: 0
        });
      }
    };

    fetchStats();
  }, [token, isAdmin]);

  if (!stats) return <div className="text-center py-8">Loading...</div>;

  // Prepare data for pie chart
  const pieData = [
    { name: 'Passed', value: stats.passed, color: '#10b981' },
    { name: 'Failed', value: stats.failed, color: '#ef4444' },
    { name: 'Pending', value: stats.pending, color: '#f59e0b' }
  ].filter(item => item.value > 0); // Only show segments with data

  const hasData = stats.passed > 0 || stats.failed > 0 || stats.pending > 0;

  return (
    <div className="space-y-6">
      <h2 className="text-2xl font-bold text-gray-800">
        {isAdmin ? 'Admin Dashboard' : 'My Dashboard'}
      </h2>
      
      {/* Info banner for users */}
      {!isAdmin && (
        <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
          <p className="text-blue-800 text-sm">
            ℹ️ Statistics calculated from {stats.suiteCount || 0} test suite{stats.suiteCount !== 1 ? 's' : ''} 
            {stats.totalTestCases > 0 && ` containing ${stats.totalTestCases} test case${stats.totalTestCases !== 1 ? 's' : ''}`}
          </p>
        </div>
      )}

      {/* Pie Chart Section */}
      <div className="bg-white rounded-lg shadow p-6">
        <h3 className="text-xl font-semibold mb-6 text-gray-800">Test Results Overview</h3>
        
        {hasData ? (
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 items-center">
            {/* Pie Chart */}
            <div className="h-80">
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={pieData}
                    cx="50%"
                    cy="50%"
                    labelLine={false}
                    label={({ name, value, percent }) => 
                      `${name}: ${value} (${(percent * 100).toFixed(1)}%)`
                    }
                    outerRadius={100}
                    fill="#8884d8"
                    dataKey="value"
                  >
                    {pieData.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={entry.color} />
                    ))}
                  </Pie>
                  <Tooltip />
                  <Legend />
                </PieChart>
              </ResponsiveContainer>
            </div>

            {/* Statistics Cards */}
            <div className="grid grid-cols-2 gap-4">
              <div className="bg-green-50 border border-green-200 rounded-lg p-6 text-center">
                <div className="text-green-600 text-sm font-semibold uppercase mb-2">Passed</div>
                <div className="text-4xl font-bold text-green-700">{stats.passed}</div>
              </div>
              
              <div className="bg-red-50 border border-red-200 rounded-lg p-6 text-center">
                <div className="text-red-600 text-sm font-semibold uppercase mb-2">Failed</div>
                <div className="text-4xl font-bold text-red-700">{stats.failed}</div>
              </div>
              
              <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-6 text-center">
                <div className="text-yellow-600 text-sm font-semibold uppercase mb-2">Pending</div>
                <div className="text-4xl font-bold text-yellow-700">{stats.pending}</div>
              </div>
              
              <div className="bg-purple-50 border border-purple-200 rounded-lg p-6 text-center">
                <div className="text-purple-600 text-sm font-semibold uppercase mb-2">Pass Rate</div>
                <div className="text-4xl font-bold text-purple-700">{stats.passRate.toFixed(1)}%</div>
              </div>
            </div>
          </div>
        ) : (
          <div className="text-center py-12 text-gray-500">
            <div className="text-6xl mb-4">📊</div>
            <p className="text-lg">No test results yet</p>
            <p className="text-sm mt-2">Execute your test suites to see the results here</p>
          </div>
        )}
      </div>
      
      {/* Suite Overview - Only show for users */}
      {!isAdmin && stats.suiteCount > 0 && (
        <div className="bg-white rounded-lg shadow p-6">
          <h3 className="text-lg font-semibold mb-4 text-gray-800">
            📋 Test Suite Overview
          </h3>
          <div className="grid grid-cols-2 gap-6">
            <div className="flex justify-between items-center">
              <span className="text-gray-600">Total Suites</span>
              <span className="font-semibold text-gray-800 text-2xl">
                {stats.suiteCount}
              </span>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-gray-600">Total Test Cases</span>
              <span className="font-semibold text-gray-800 text-2xl">
                {stats.totalTestCases || 0}
              </span>
            </div>
          </div>
        </div>
      )}

      {/* Empty state - No suites created */}
      {!isAdmin && stats.suiteCount === 0 && (
        <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-6 text-center">
          <div className="text-4xl mb-4">🚀</div>
          <h3 className="text-xl font-semibold text-yellow-900 mb-2">
            Ready to Start Testing?
          </h3>
          <p className="text-yellow-800 mb-4">
            You haven't created any test suites yet. Get started by creating your first test suite!
          </p>
          <div className="flex gap-4 justify-center">
            <button 
              className="px-6 py-2 bg-yellow-600 text-white rounded-lg hover:bg-yellow-700 transition font-medium"
            >
              📋 Create Test Suite
            </button>
            <button 
              className="px-6 py-2 bg-yellow-600 text-white rounded-lg hover:bg-yellow-700 transition font-medium"
            >
              🧪 Run Single Test
            </button>
          </div>
        </div>
      )}

      {/* Message when suites exist but not executed */}
      {!isAdmin && stats.suiteCount > 0 && !hasData && (
        <div className="bg-blue-50 border border-blue-200 rounded-lg p-6 text-center">
          <div className="text-4xl mb-4">⏳</div>
          <h3 className="text-xl font-semibold text-blue-900 mb-2">
            Test Suites Ready to Execute
          </h3>
          <p className="text-blue-800 mb-4">
            You have {stats.suiteCount} test suite{stats.suiteCount !== 1 ? 's' : ''} with {stats.totalTestCases} test case{stats.totalTestCases !== 1 ? 's' : ''}, 
            but they haven't been executed yet.
          </p>
          <button 
            className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition font-medium"
          >
            ▶️ Execute Test Suites
          </button>
        </div>
      )}
      
      {/* Admin-only educational content */}
      {isAdmin && (
        <div className="bg-white rounded-lg shadow p-6">
          <h3 className="text-xl font-semibold mb-4 text-gray-800">Why Automated Testing Matters</h3>
          <div className="space-y-4 text-gray-700">
            <div>
              <h4 className="font-semibold text-lg mb-2 flex items-center gap-2">
                <span className="text-blue-600">🚀</span> Faster Release Cycles
              </h4>
              <p className="text-sm leading-relaxed">
                Automated testing dramatically reduces the time required for regression testing, allowing teams to deploy new features and updates more frequently. What once took days of manual testing can now be completed in minutes, enabling continuous integration and deployment practices.
              </p>
            </div>
            
            <div>
              <h4 className="font-semibold text-lg mb-2 flex items-center gap-2">
                <span className="text-green-600">💰</span> Cost Efficiency
              </h4>
              <p className="text-sm leading-relaxed">
                While there's an initial investment in setting up automated tests, the long-term savings are substantial. Automated tests can run thousands of times without additional cost, catch bugs early when they're cheaper to fix, and free up QA teams to focus on exploratory testing and complex scenarios that require human insight.
              </p>
            </div>
            
            <div>
              <h4 className="font-semibold text-lg mb-2 flex items-center gap-2">
                <span className="text-purple-600">🎯</span> Improved Accuracy & Consistency
              </h4>
              <p className="text-sm leading-relaxed">
                Human testers can make mistakes, especially when performing repetitive tasks. Automated tests execute the same steps precisely every time, eliminating human error and providing consistent, reliable results. This consistency is crucial for maintaining quality standards across multiple releases.
              </p>
            </div>
            
            <div>
              <h4 className="font-semibold text-lg mb-2 flex items-center gap-2">
                <span className="text-red-600">🛡️</span> Enhanced Code Quality & Confidence
              </h4>
              <p className="text-sm leading-relaxed">
                Comprehensive automated test coverage gives developers the confidence to refactor code and make improvements without fear of breaking existing functionality. This safety net encourages better code practices, reduces technical debt, and ultimately leads to more maintainable and robust applications.
              </p>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Dashboard;