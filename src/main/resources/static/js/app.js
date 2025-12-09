/*
  Vanilla Template - Application Logic
  Homepage & Upload Flow with Authentication
*/

(function($) {
  'use strict';

  // State management
  const app = {
    currentUser: null,
    validatedUrl: null,
    
    init: function() {
      this.checkSession();
      this.bindEvents();
      this.addStyles();
    },

    // Check if user is already logged in (from localStorage)
    checkSession: function() {
      const session = localStorage.getItem('userSession');
      if (session) {
        try {
          this.currentUser = JSON.parse(session);
          this.displayUserWelcome();
        } catch (e) {
          localStorage.removeItem('userSession');
        }
      }
    },

    // Display welcome message if user is logged in
    displayUserWelcome: function() {
      const userDisplay = document.getElementById('user-display');
      const defaultTagline = document.getElementById('default-tagline');
      const userName = document.getElementById('user-name');
      
      if (userDisplay && userName) {
        userName.textContent = this.currentUser.email || this.currentUser.name;
        userDisplay.style.display = 'inline-block';
        defaultTagline.style.display = 'none';
      }
    },

    // Hide welcome message and show default tagline
    hideUserWelcome: function() {
      const userDisplay = document.getElementById('user-display');
      const defaultTagline = document.getElementById('default-tagline');
      
      if (userDisplay && defaultTagline) {
        userDisplay.style.display = 'none';
        defaultTagline.style.display = 'inline-block';
      }
    },

    // Bind all event listeners
    bindEvents: function() {
      const self = this;

      // URL Input and Scan Button
      const scanBtn = document.getElementById('scan-button');
      const urlInput = document.getElementById('auctionUrl');
      
      if (scanBtn) {
        scanBtn.addEventListener('click', function() {
          self.validateAndScanUrl();
        });
      }

      if (urlInput) {
        urlInput.addEventListener('keypress', function(e) {
          if (e.key === 'Enter') {
            self.validateAndScanUrl();
          }
        });
      }

      // File Upload
      const fileBtn = document.getElementById('file-button');
      const pdfFile = document.getElementById('pdfFile');
      const uploadBtn = document.getElementById('upload-button');
      
      if (fileBtn && pdfFile) {
        fileBtn.addEventListener('click', function() {
          pdfFile.click();
        });
      }

      if (pdfFile) {
        pdfFile.addEventListener('change', function() {
          self.handleFileSelect();
        });
      }

      if (uploadBtn) {
        uploadBtn.addEventListener('click', function() {
          self.handleFileUpload();
        });
      }

      // Auth Modal
      const toRegisterLink = document.getElementById('to-register-link');
      const toLoginLink = document.getElementById('to-login-link');
      const loginBtn = document.getElementById('login-btn');
      const registerBtn = document.getElementById('register-btn');
      const registerPassword = document.getElementById('register-password');
      const registerConfirm = document.getElementById('register-confirm');
      
      if (toRegisterLink) {
        toRegisterLink.addEventListener('click', function(e) {
          e.preventDefault();
          self.showRegisterPanel();
        });
      }

      if (toLoginLink) {
        toLoginLink.addEventListener('click', function(e) {
          e.preventDefault();
          self.showLoginPanel();
        });
      }

      if (loginBtn) {
        loginBtn.addEventListener('click', function() {
          self.handleLogin();
        });
      }

      if (registerBtn) {
        registerBtn.addEventListener('click', function() {
          self.handleRegister();
        });
      }

      // Password strength validation
      if (registerPassword) {
        registerPassword.addEventListener('input', function() {
          self.checkPasswordStrength();
        });
      }

      // Password match validation
      if (registerConfirm) {
        registerConfirm.addEventListener('input', function() {
          self.checkPasswordMatch();
        });
      }

      // Close auth overlay on background click
      const authOverlay = document.getElementById('auth-overlay');
      if (authOverlay) {
        authOverlay.addEventListener('click', function(e) {
          if (e.target === authOverlay) {
            // Allow closing only if user is already authenticated
            // Prevent closing if trying to access PDF upload without auth
          }
        });
      }
    },

    // URL Validation
    validateAndScanUrl: function() {
      const urlInput = document.getElementById('auctionUrl').value.trim();
      const urlError = document.getElementById('url-error');

      urlError.style.display = 'none';

      if (!urlInput) {
        this.showUrlError('Please enter a URL');
        return;
      }

      // Basic URL validation
      if (!this.isValidUrl(urlInput)) {
        this.showUrlError('Please enter a valid URL');
        return;
      }

      // Simulate URL validation (in production, this would be an API call)
      this.validatedUrl = urlInput;

      // Check if user is logged in
      if (!this.currentUser) {
        // Show auth modal if not logged in
        this.showAuthOverlay();
      } else {
        // Show PDF upload section
        this.showPdfUploadSection();
      }
    },

    // URL validation helper
    isValidUrl: function(string) {
      try {
        new URL(string);
        return true;
      } catch (_) {
        return false;
      }
    },

    // Show URL error
    showUrlError: function(message) {
      const urlError = document.getElementById('url-error');
      urlError.innerHTML = message;
      urlError.style.display = 'block';
      
      // Auto-hide after 5 seconds
      setTimeout(function() {
        urlError.style.display = 'none';
      }, 5000);
    },

    // Show PDF Upload Section
    showPdfUploadSection: function() {
      const urlSection = document.getElementById('url-input-section');
      const pdfSection = document.getElementById('pdf-upload-section');
      const originalBtn = document.getElementById('original-button');

      // Fade out url section
      if (urlSection) {
        urlSection.style.opacity = '0.5';
        urlSection.style.pointerEvents = 'none';
      }

      if (pdfSection) {
        pdfSection.style.display = 'block';
      }

      if (originalBtn) {
        originalBtn.style.display = 'none';
      }
    },

    // Handle file selection
    handleFileSelect: function() {
      const pdfFile = document.getElementById('pdfFile');
      const fileName = document.getElementById('file-name');
      const fileError = document.getElementById('file-error');
      const uploadBtn = document.getElementById('upload-button');

      if (!pdfFile.files || pdfFile.files.length === 0) {
        return;
      }

      const file = pdfFile.files[0];

      // Validate file type
      if (file.type !== 'application/pdf') {
        fileError.innerHTML = 'Please select a valid PDF file';
        fileError.style.display = 'block';
        return;
      }

      // Validate file size (max 50MB)
      const maxSize = 50 * 1024 * 1024;
      if (file.size > maxSize) {
        fileError.innerHTML = 'File size exceeds 50MB limit';
        fileError.style.display = 'block';
        return;
      }

      fileError.style.display = 'none';
      fileName.innerHTML = '✓ ' + file.name + ' selected';
      uploadBtn.style.display = 'block';
    },

    // Handle file upload
    handleFileUpload: function() {
      const pdfFile = document.getElementById('pdfFile');
      
      if (!pdfFile.files || pdfFile.files.length === 0) {
        return;
      }

      const file = pdfFile.files[0];
      const uploadBtn = document.getElementById('upload-button');
      const originalBtnText = uploadBtn.innerHTML;

      // Show loading state
      uploadBtn.disabled = true;
      uploadBtn.innerHTML = 'Processing...';

      // Simulate file upload and processing
      setTimeout(() => {
        // In production, this would be an actual API call
        this.processUploadSuccess(file.name);
        uploadBtn.innerHTML = originalBtnText;
        uploadBtn.disabled = false;
      }, 2000);
    },

    // Process successful upload
    processUploadSuccess: function(fileName) {
      // Show success message
      const pdfSection = document.getElementById('pdf-upload-section');
      const successMsg = document.createElement('div');
      successMsg.className = 'alert alert-success';
      successMsg.innerHTML = '✓ ' + fileName + ' uploaded successfully! Processing your auction data...';
      successMsg.style.marginTop = '15px';
      successMsg.style.animation = 'slideInDown 0.4s ease-out';
      
      const container = pdfSection.querySelector('.upload-container');
      container.appendChild(successMsg);

      // Simulate processing
      setTimeout(() => {
        this.showProcessingResults();
      }, 1500);
    },

    // Show processing results
    showProcessingResults: function() {
      const pdfSection = document.getElementById('pdf-upload-section');
      const container = pdfSection.querySelector('.upload-container');
      
      container.innerHTML = `
        <h4 style="color: #fff; margin-bottom: 20px;">Auction Analysis Results</h4>
        <div style="background-color: rgba(255, 255, 255, 0.1); padding: 20px; border-radius: 5px; color: #fff; margin-bottom: 20px;">
          <p><strong>URL:</strong> ${this.validatedUrl}</p>
          <p><strong>Status:</strong> <span style="color: #4CAF50;">✓ Successfully processed</span></p>
          <p><strong>Items Found:</strong> 5-10 items detected</p>
          <p><strong>Processing Time:</strong> ~2 seconds</p>
        </div>
        <button id="process-again" class="btn btn-primary" style="background-color: #ff7d27; padding: 15px 24px; border-radius: 3px; font-weight: 700; text-transform: uppercase; font-size: 11px; border: none; margin-right: 10px; color: #fff;">Process Another</button>
        <button id="view-feedback" class="btn btn-primary" style="background-color: #666; padding: 15px 24px; border-radius: 3px; font-weight: 700; text-transform: uppercase; font-size: 11px; border: none; color: #fff;">View Feedback</button>
      `;

      const processAgainBtn = document.getElementById('process-again');
      const viewFeedbackBtn = document.getElementById('view-feedback');

      if (processAgainBtn) {
        processAgainBtn.addEventListener('click', () => {
          this.resetUploadFlow();
        });
      }

      if (viewFeedbackBtn) {
        viewFeedbackBtn.addEventListener('click', () => {
          alert('Feedback generation feature - would show detailed analysis here');
        });
      }
    },

    // Reset upload flow
    resetUploadFlow: function() {
      document.getElementById('auctionUrl').value = '';
      document.getElementById('pdfFile').value = '';
      document.getElementById('file-name').innerHTML = '';
      document.getElementById('pdf-upload-section').style.display = 'none';
      
      const urlSection = document.getElementById('url-input-section');
      if (urlSection) {
        urlSection.style.opacity = '1';
        urlSection.style.pointerEvents = 'auto';
      }

      this.validatedUrl = null;
    },

    // Authentication - Show Auth Overlay
    showAuthOverlay: function() {
      const overlay = document.getElementById('auth-overlay');
      if (overlay) {
        overlay.style.display = 'flex';
        this.showLoginPanel();
      }
    },

    // Show login panel
    showLoginPanel: function() {
      const loginPanel = document.getElementById('login-panel');
      const registerPanel = document.getElementById('register-panel');
      
      if (loginPanel) loginPanel.style.display = 'block';
      if (registerPanel) registerPanel.style.display = 'none';
    },

    // Show register panel
    showRegisterPanel: function() {
      const loginPanel = document.getElementById('login-panel');
      const registerPanel = document.getElementById('register-panel');
      
      if (loginPanel) loginPanel.style.display = 'none';
      if (registerPanel) registerPanel.style.display = 'block';
    },

    // Handle login
    handleLogin: function() {
      const email = document.getElementById('login-email').value.trim();
      const password = document.getElementById('login-password').value.trim();

      if (!email || !password) {
        alert('Please fill in all fields');
        return;
      }

      if (!this.isValidEmail(email)) {
        alert('Please enter a valid email address');
        return;
      }

      // Simulate login
      this.currentUser = {
        email: email,
        name: email.split('@')[0]
      };

      // Save session
      localStorage.setItem('userSession', JSON.stringify(this.currentUser));

      // Close auth modal
      this.closeAuthOverlay();

      // Show user welcome
      this.displayUserWelcome();

      // Show PDF upload section
      this.showPdfUploadSection();

      // Clear form
      document.getElementById('login-form').reset();
    },

    // Handle registration
    handleRegister: function() {
      const email = document.getElementById('register-email').value.trim();
      const phone = document.getElementById('register-phone').value.trim();
      const password = document.getElementById('register-password').value.trim();
      const confirm = document.getElementById('register-confirm').value.trim();

      if (!email || !phone || !password || !confirm) {
        alert('Please fill in all fields');
        return;
      }

      if (!this.isValidEmail(email)) {
        alert('Please enter a valid email address');
        return;
      }

      if (!this.isValidPhone(phone)) {
        alert('Please enter a valid phone number');
        return;
      }

      if (password !== confirm) {
        alert('Passwords do not match');
        return;
      }

      if (!this.isStrongPassword(password)) {
        alert('Password must be at least 8 characters with uppercase, lowercase, and numbers');
        return;
      }

      // Simulate registration
      this.currentUser = {
        email: email,
        phone: phone,
        name: email.split('@')[0]
      };

      // Save session
      localStorage.setItem('userSession', JSON.stringify(this.currentUser));

      // Close auth modal
      this.closeAuthOverlay();

      // Show user welcome
      this.displayUserWelcome();

      // Show PDF upload section
      this.showPdfUploadSection();

      // Clear form
      document.getElementById('register-form').reset();
    },

    // Close auth overlay
    closeAuthOverlay: function() {
      const overlay = document.getElementById('auth-overlay');
      if (overlay) {
        overlay.style.display = 'none';
      }
    },

    // Password strength checker
    checkPasswordStrength: function() {
      const password = document.getElementById('register-password').value;
      const strengthDisplay = document.getElementById('password-strength');

      let strength = 0;
      let feedback = '';

      if (password.length >= 8) strength++;
      if (/[a-z]/.test(password)) strength++;
      if (/[A-Z]/.test(password)) strength++;
      if (/[0-9]/.test(password)) strength++;
      if (/[^a-zA-Z0-9]/.test(password)) strength++;

      const strengthLevels = [
        { label: '', color: '#ccc' },
        { label: 'Weak', color: '#d32f2f' },
        { label: 'Fair', color: '#f57c00' },
        { label: 'Good', color: '#fbc02d' },
        { label: 'Strong', color: '#388e3c' },
        { label: 'Very Strong', color: '#1976d2' }
      ];

      const level = strengthLevels[strength];
      if (password) {
        strengthDisplay.innerHTML = `<span style="color: ${level.color};">● ${level.label}</span>`;
      } else {
        strengthDisplay.innerHTML = '';
      }
    },

    // Password match checker
    checkPasswordMatch: function() {
      const password = document.getElementById('register-password').value;
      const confirm = document.getElementById('register-confirm').value;
      const matchDisplay = document.getElementById('password-match');

      if (confirm) {
        if (password === confirm) {
          matchDisplay.innerHTML = '<span style="color: #4CAF50;">✓ Passwords match</span>';
        } else {
          matchDisplay.innerHTML = '<span style="color: #d32f2f;">✗ Passwords do not match</span>';
        }
      } else {
        matchDisplay.innerHTML = '';
      }
    },

    // Email validation
    isValidEmail: function(email) {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      return emailRegex.test(email);
    },

    // Phone validation
    isValidPhone: function(phone) {
      const phoneRegex = /^\d{10,15}$/;
      return phoneRegex.test(phone.replace(/\D/g, ''));
    },

    // Password strength validation
    isStrongPassword: function(password) {
      return password.length >= 8 &&
             /[a-z]/.test(password) &&
             /[A-Z]/.test(password) &&
             /[0-9]/.test(password);
    },

    // Add custom styles for auth modal and animations
    addStyles: function() {
      const style = document.createElement('style');
      style.textContent = `
        @keyframes slideInDown {
          from {
            opacity: 0;
            transform: translateY(-20px);
          }
          to {
            opacity: 1;
            transform: translateY(0);
          }
        }

        @keyframes slideInUp {
          from {
            opacity: 0;
            transform: translateY(20px);
          }
          to {
            opacity: 1;
            transform: translateY(0);
          }
        }

        .auth-overlay {
          position: fixed;
          top: 0;
          left: 0;
          right: 0;
          bottom: 0;
          background-color: rgba(0, 0, 0, 0.8);
          display: flex;
          align-items: center;
          justify-content: center;
          z-index: 10000;
          animation: fadeIn 0.3s ease-out;
        }

        @keyframes fadeIn {
          from {
            opacity: 0;
          }
          to {
            opacity: 1;
          }
        }

        .auth-modal {
          background-color: #fff;
          padding: 40px;
          border-radius: 5px;
          box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
          width: 90%;
          max-width: 400px;
          animation: slideInUp 0.4s ease-out;
        }

        .auth-panel h3 {
          color: #333;
          margin-bottom: 25px;
          font-weight: 600;
          font-size: 24px;
        }

        .auth-panel .form-group {
          margin-bottom: 15px;
        }

        .auth-panel .form-control {
          border: 1px solid #ddd;
          box-shadow: none;
        }

        .auth-panel .form-control:focus {
          border-color: #ff7d27;
          box-shadow: 0 0 0 0.2rem rgba(255, 125, 39, 0.25);
        }

        .url-input-wrapper .input-group .form-control {
          border: 1px solid #ddd;
        }

        .url-input-wrapper .input-group .form-control:focus {
          border-color: #ff7d27;
          box-shadow: none;
        }

        .upload-container h4 {
          font-size: 20px;
          font-weight: 600;
        }

        .alert {
          border: none;
        }

        .alert-danger {
          background-color: rgba(211, 47, 47, 0.1);
          color: #d32f2f;
          border-radius: 3px;
        }

        .alert-success {
          background-color: rgba(76, 175, 80, 0.1);
          color: #4CAF50;
          border-radius: 3px;
        }
      `;
      document.head.appendChild(style);
    }
  };

  // Initialize app when DOM is ready
  $(document).ready(function() {
    app.init();

    // Keep the original smooth scroll functionality
    $(".fixed-side-navbar a, .primary-button a").on('click', function(event) {
      if (this.hash !== "") {
        event.preventDefault();
        var hash = this.hash;
        $('html, body').animate({
          scrollTop: $(hash).offset().top
        }, 800, function(){
          window.location.hash = hash;
        });
      }
    });
  });

})(jQuery);
