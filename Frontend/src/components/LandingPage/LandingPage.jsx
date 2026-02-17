import React from "react";
import { useNavigate } from "react-router-dom";

const LandingPage = () => {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-blue-900 to-slate-900 text-white">
      
      {/* Navbar */}
      <nav className="flex justify-between items-center px-10 py-6">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 bg-blue-600 rounded-lg flex items-center justify-center font-bold">
            TF
          </div>
          <h1 className="text-xl font-bold">Test Framework</h1>
        </div>

        <div className="flex gap-4">
          <button
            onClick={() => navigate("/login")}
            className="px-5 py-2 border border-blue-400 rounded-lg hover:bg-blue-600 transition"
          >
            Sign In
          </button>
          <button
           onClick={() => navigate("/login")}
            className="px-5 py-2 bg-blue-600 rounded-lg hover:bg-blue-700 transition"
          >
            Get Started
          </button>
        </div>
      </nav>

      {/* Hero Section */}
      <div className="flex flex-col items-center text-center mt-20 px-6">
        <h2 className="text-5xl font-bold leading-tight max-w-3xl">
          Enterprise-Grade Automated Testing Platform
        </h2>
        <p className="text-slate-300 mt-6 text-lg max-w-2xl">
          Execute, monitor, and analyze your UI and API test suites with real-time
          metrics, flaky test detection, and intelligent reporting.
        </p>

        <div className="flex gap-6 mt-10">
          <button
            onClick={() => navigate("/login")}
            className="px-8 py-3 bg-blue-600 rounded-lg text-lg hover:bg-blue-700 transition shadow-lg"
          >
            🚀 Start Testing
          </button>
        </div>
      </div>

      {/* Features Section */}
      <div className="grid md:grid-cols-3 gap-8 px-10 mt-24 mb-20">
        
        <div className="bg-slate-800 p-8 rounded-xl shadow-lg">
          <h3 className="text-xl font-semibold mb-3">⚡ Parallel Execution</h3>
          <p className="text-slate-300 text-sm">
            Run UI and API tests concurrently with isolated thread pools for
            optimized performance and stability.
          </p>
        </div>

        <div className="bg-slate-800 p-8 rounded-xl shadow-lg">
          <h3 className="text-xl font-semibold mb-3">📊 Real-Time Metrics</h3>
          <p className="text-slate-300 text-sm">
            Track pass rate, execution time, flaky tests, and performance trends
            through dynamic dashboards.
          </p>
        </div>

        <div className="bg-slate-800 p-8 rounded-xl shadow-lg">
          <h3 className="text-xl font-semibold mb-3">🔐 Secure & Role-Based</h3>
          <p className="text-slate-300 text-sm">
            JWT-secured authentication with Admin and User role-based access
            control.
          </p>
        </div>

      </div>

      {/* Footer */}
      <footer className="text-center text-slate-400 text-sm pb-8 border-t border-slate-700 pt-6">
        © 2026 Test Framework • Built with Spring Boot & React
      </footer>
    </div>
  );
};

export default LandingPage;
