/* ===================================
   WAR OF WAVES - JAVASCRIPT
   College Web Development Project
   =================================== */

// DOM Ready
document.addEventListener('DOMContentLoaded', function() {
    // Initialize all features
    initMobileMenu();
    initScrollReveal();
    initCounterAnimation();
    initSmoothScroll();
    initFaqToggle();
    initOsTabs();
    initContactForm();
    initNavbarScroll();
});

// ===================================
// MOBILE MENU
// ===================================
function initMobileMenu() {
    const mobileBtn = document.querySelector('.mobile-menu-btn');
    const navLinks = document.querySelector('.nav-links');

    if (mobileBtn && navLinks) {
        mobileBtn.addEventListener('click', function() {
            navLinks.classList.toggle('active');
        });

        // Close menu when clicking a link
        navLinks.querySelectorAll('a').forEach(link => {
            link.addEventListener('click', function() {
                navLinks.classList.remove('active');
            });
        });
    }
}

// Global toggle function for inline onclick
function toggleMobileMenu() {
    const navLinks = document.querySelector('.nav-links');
    if (navLinks) {
        navLinks.classList.toggle('active');
    }
}

// ===================================
// NAVBAR SCROLL EFFECT
// ===================================
function initNavbarScroll() {
    const navbar = document.querySelector('.navbar');
    
    if (navbar) {
        window.addEventListener('scroll', function() {
            if (window.scrollY > 50) {
                navbar.style.background = 'rgba(10, 10, 10, 0.98)';
                navbar.style.boxShadow = '0 2px 20px rgba(0, 0, 0, 0.3)';
            } else {
                navbar.style.background = 'rgba(10, 10, 10, 0.95)';
                navbar.style.boxShadow = 'none';
            }
        });
    }
}

// ===================================
// SCROLL REVEAL ANIMATION
// ===================================
function initScrollReveal() {
    // Add reveal class to elements
    const revealElements = document.querySelectorAll(
        '.feature-card, .stat-card, .control-card, .tip-card, .wave-card, ' +
        '.download-card, .learning-card, .tech-card, .info-card, .stack-item, ' +
        '.timeline-item, .feature-item, .arch-node'
    );

    revealElements.forEach(el => {
        el.classList.add('reveal');
    });

    // Check if element is in viewport
    function checkReveal() {
        revealElements.forEach(el => {
            const windowHeight = window.innerHeight;
            const elementTop = el.getBoundingClientRect().top;
            const revealPoint = 150;

            if (elementTop < windowHeight - revealPoint) {
                el.classList.add('visible');
            }
        });
    }

    // Initial check
    checkReveal();

    // Check on scroll
    window.addEventListener('scroll', checkReveal);
}

// ===================================
// COUNTER ANIMATION
// ===================================
function initCounterAnimation() {
    const counters = document.querySelectorAll('.stat-number');
    let hasAnimated = false;

    function animateCounters() {
        counters.forEach(counter => {
            const target = parseInt(counter.getAttribute('data-target'));
            const duration = 2000; // 2 seconds
            const step = target / (duration / 16); // 60fps
            let current = 0;

            const updateCounter = () => {
                current += step;
                if (current < target) {
                    counter.textContent = Math.floor(current);
                    requestAnimationFrame(updateCounter);
                } else {
                    counter.textContent = target;
                }
            };

            updateCounter();
        });
    }

    // Start animation when stats section is visible
    function checkCounterVisibility() {
        const statsSection = document.querySelector('.stats');
        if (statsSection && !hasAnimated) {
            const rect = statsSection.getBoundingClientRect();
            if (rect.top < window.innerHeight && rect.bottom > 0) {
                hasAnimated = true;
                animateCounters();
            }
        }
    }

    window.addEventListener('scroll', checkCounterVisibility);
    checkCounterVisibility(); // Initial check
}

// ===================================
// SMOOTH SCROLL
// ===================================
function initSmoothScroll() {
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function(e) {
            const href = this.getAttribute('href');
            if (href !== '#') {
                e.preventDefault();
                const target = document.querySelector(href);
                if (target) {
                    target.scrollIntoView({
                        behavior: 'smooth',
                        block: 'start'
                    });
                }
            }
        });
    });
}

// ===================================
// FAQ TOGGLE
// ===================================
function initFaqToggle() {
    // This is handled by inline onclick, but we can enhance it
}

// Global toggle function for inline onclick
function toggleFaq(element) {
    const faqItem = element.parentElement;
    const allItems = document.querySelectorAll('.faq-item');
    
    // Close other items
    allItems.forEach(item => {
        if (item !== faqItem) {
            item.classList.remove('open');
        }
    });
    
    // Toggle current item
    faqItem.classList.toggle('open');
}

// ===================================
// OS TABS (Download page)
// ===================================
function initOsTabs() {
    // Initial state is handled by CSS classes
}

// Global function for OS tab switching
function showTab(tabId) {
    // Update tab buttons
    const tabs = document.querySelectorAll('.os-tab');
    tabs.forEach(tab => {
        tab.classList.remove('active');
        if (tab.textContent.toLowerCase().includes(tabId === 'windows' ? 'windows' : 'macos')) {
            tab.classList.add('active');
        }
    });

    // Update code blocks
    const codeBlocks = document.querySelectorAll('.os-code');
    codeBlocks.forEach(block => {
        block.classList.remove('active');
    });

    const activeBlock = document.getElementById(tabId);
    if (activeBlock) {
        activeBlock.classList.add('active');
    }
}

// ===================================
// CONTACT FORM
// ===================================
function initContactForm() {
    // Form is handled by handleSubmit function
}

// Global function for form submission
function handleSubmit(event) {
    event.preventDefault();
    
    const form = event.target;
    const name = form.querySelector('#name').value;
    const email = form.querySelector('#email').value;
    const message = form.querySelector('#message').value;
    
    // Simple validation
    if (!name || !email || !message) {
        alert('Please fill in all fields.');
        return;
    }
    
    // Simulate form submission
    const submitBtn = form.querySelector('button[type="submit"]');
    const originalText = submitBtn.textContent;
    
    submitBtn.textContent = 'Sending...';
    submitBtn.disabled = true;
    
    // Simulate API call
    setTimeout(() => {
        alert('Thank you for your message! This is a demo form for the college project.');
        form.reset();
        submitBtn.textContent = originalText;
        submitBtn.disabled = false;
    }, 1500);
}

// ===================================
// TYPING EFFECT (Optional enhancement)
// ===================================
function typeWriter(element, text, speed = 50) {
    let i = 0;
    element.textContent = '';
    
    function type() {
        if (i < text.length) {
            element.textContent += text.charAt(i);
            i++;
            setTimeout(type, speed);
        }
    }
    
    type();
}

// ===================================
// PARALLAX EFFECT (Optional enhancement)
// ===================================
function initParallax() {
    const hero = document.querySelector('.hero');
    
    if (hero) {
        window.addEventListener('scroll', function() {
            const scrolled = window.pageYOffset;
            hero.style.backgroundPositionY = scrolled * 0.5 + 'px';
        });
    }
}

// ===================================
// CURSOR TRAIL EFFECT (Optional fun feature)
// ===================================
function initCursorTrail() {
    const trail = [];
    const trailLength = 10;
    
    for (let i = 0; i < trailLength; i++) {
        const dot = document.createElement('div');
        dot.className = 'cursor-trail-dot';
        dot.style.cssText = `
            position: fixed;
            width: ${10 - i}px;
            height: ${10 - i}px;
            background: rgba(255, 68, 68, ${1 - i * 0.1});
            border-radius: 50%;
            pointer-events: none;
            z-index: 9999;
            transition: transform 0.1s ease;
        `;
        document.body.appendChild(dot);
        trail.push(dot);
    }
    
    let mouseX = 0, mouseY = 0;
    
    document.addEventListener('mousemove', function(e) {
        mouseX = e.clientX;
        mouseY = e.clientY;
    });
    
    function animate() {
        let x = mouseX, y = mouseY;
        
        trail.forEach((dot, index) => {
            dot.style.left = x + 'px';
            dot.style.top = y + 'px';
            dot.style.transform = `translate(-50%, -50%)`;
            
            const nextDot = trail[index + 1] || trail[0];
            x += (parseFloat(nextDot.style.left) || x) - x;
            y += (parseFloat(nextDot.style.top) || y) - y;
        });
        
        requestAnimationFrame(animate);
    }
    
    // Uncomment to enable cursor trail
    // animate();
}

// ===================================
// UTILITY FUNCTIONS
// ===================================

// Debounce function for performance
function debounce(func, wait = 20) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// Check if element is in viewport
function isInViewport(element) {
    const rect = element.getBoundingClientRect();
    return (
        rect.top >= 0 &&
        rect.left >= 0 &&
        rect.bottom <= (window.innerHeight || document.documentElement.clientHeight) &&
        rect.right <= (window.innerWidth || document.documentElement.clientWidth)
    );
}

// Get current page
function getCurrentPage() {
    const path = window.location.pathname;
    const page = path.split('/').pop() || 'index.html';
    return page;
}

// Set active nav link
function setActiveNavLink() {
    const currentPage = getCurrentPage();
    const navLinks = document.querySelectorAll('.nav-links a');
    
    navLinks.forEach(link => {
        link.classList.remove('active');
        if (link.getAttribute('href') === currentPage) {
            link.classList.add('active');
        }
    });
}

// Initialize active nav link on page load
setActiveNavLink();

// ===================================
// CONSOLE EASTER EGG
// ===================================
console.log(`
%c⚔️ WAR OF WAVES ⚔️
%cA 3D Wave-Based Shooter Game

%c🎮 Built with Java & jMonkeyEngine
🌐 Website built with HTML, CSS, JavaScript

%cCollege Web Development Project
Created by Rohith G
`, 
'color: #ff4444; font-size: 24px; font-weight: bold;',
'color: #67b5ff; font-size: 14px;',
'color: #ffd700; font-size: 12px;',
'color: #888; font-size: 11px;'
);
