/**
 * EXPLORIFY - Landing Page JavaScript
 * Maneja todas las interacciones y animaciones
 */

(function() {
    'use strict';

    // ========================================
    // PARTICLES ANIMATION
    // ========================================
    class ParticlesAnimation {
        constructor(canvasId) {
            this.canvas = document.getElementById(canvasId);
            if (!this.canvas) return;
            
            this.ctx = this.canvas.getContext('2d');
            this.particlesArray = [];
            this.numberOfParticles = window.innerWidth < 768 ? 50 : 100;
            
            this.init();
            this.setupEventListeners();
        }
        
        init() {
            this.resizeCanvas();
            this.createParticles();
            this.animate();
        }
        
        setupEventListeners() {
            window.addEventListener('resize', () => this.resizeCanvas());
        }
        
        resizeCanvas() {
            this.canvas.width = window.innerWidth;
            this.canvas.height = window.innerHeight;
        }
        
        createParticles() {
            this.particlesArray = [];
            for (let i = 0; i < this.numberOfParticles; i++) {
                this.particlesArray.push(new Particle(this.canvas));
            }
        }
        
        animate() {
            this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
            
            for (let i = 0; i < this.particlesArray.length; i++) {
                this.particlesArray[i].update();
                this.particlesArray[i].draw(this.ctx);
                
                // Connect particles
                for (let j = i; j < this.particlesArray.length; j++) {
                    const dx = this.particlesArray[i].x - this.particlesArray[j].x;
                    const dy = this.particlesArray[i].y - this.particlesArray[j].y;
                    const distance = Math.sqrt(dx * dx + dy * dy);
                    
                    if (distance < 100) {
                        this.ctx.strokeStyle = `rgba(102, 126, 234, ${0.1 * (1 - distance / 100)})`;
                        this.ctx.lineWidth = 1;
                        this.ctx.beginPath();
                        this.ctx.moveTo(this.particlesArray[i].x, this.particlesArray[i].y);
                        this.ctx.lineTo(this.particlesArray[j].x, this.particlesArray[j].y);
                        this.ctx.stroke();
                    }
                }
            }
            
            requestAnimationFrame(() => this.animate());
        }
    }

    // ========================================
    // PARTICLE CLASS
    // ========================================
    class Particle {
        constructor(canvas) {
            this.canvas = canvas;
            this.x = Math.random() * canvas.width;
            this.y = Math.random() * canvas.height;
            this.size = Math.random() * 2 + 0.5;
            this.speedX = Math.random() * 0.5 - 0.25;
            this.speedY = Math.random() * 0.5 - 0.25;
            this.opacity = Math.random() * 0.5 + 0.2;
        }
        
        update() {
            this.x += this.speedX;
            this.y += this.speedY;
            
            if (this.x > this.canvas.width) this.x = 0;
            if (this.x < 0) this.x = this.canvas.width;
            if (this.y > this.canvas.height) this.y = 0;
            if (this.y < 0) this.y = this.canvas.height;
        }
        
        draw(ctx) {
            ctx.fillStyle = `rgba(102, 126, 234, ${this.opacity})`;
            ctx.beginPath();
            ctx.arc(this.x, this.y, this.size, 0, Math.PI * 2);
            ctx.fill();
        }
    }

    // ========================================
    // SMOOTH SCROLL
    // ========================================
    class SmoothScroll {
        constructor() {
            this.init();
        }
        
        init() {
            document.querySelectorAll('a[href^="#"]').forEach(anchor => {
                anchor.addEventListener('click', (e) => {
                    const href = anchor.getAttribute('href');
                    if (href === '#') return;
                    
                    e.preventDefault();
                    const target = document.querySelector(href);
                    
                    if (target) {
                        target.scrollIntoView({
                            behavior: 'smooth',
                            block: 'start'
                        });
                    }
                });
            });
        }
    }

    // ========================================
    // SCROLL ANIMATIONS
    // ========================================
    class ScrollAnimations {
        constructor() {
            this.observerOptions = {
                threshold: 0.1,
                rootMargin: '0px 0px -50px 0px'
            };
            
            this.init();
        }
        
        init() {
            const observer = new IntersectionObserver(
                (entries) => this.handleIntersection(entries),
                this.observerOptions
            );
            
            // Observe feature cards
            document.querySelectorAll('.feature-card').forEach((card, index) => {
                card.style.opacity = '0';
                card.style.transform = 'translateY(30px)';
                card.style.transition = `opacity 0.6s ease-out ${index * 0.1}s, transform 0.6s ease-out ${index * 0.1}s`;
                observer.observe(card);
            });
            
            // Observe other animated elements
            document.querySelectorAll('.showcase-text, .showcase-visual').forEach(el => {
                el.style.opacity = '0';
                el.style.transform = 'translateY(30px)';
                el.style.transition = 'opacity 0.8s ease-out, transform 0.8s ease-out';
                observer.observe(el);
            });
        }
        
        handleIntersection(entries) {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    entry.target.style.opacity = '1';
                    entry.target.style.transform = 'translateY(0)';
                }
            });
        }
    }

    // ========================================
    // STATS COUNTER ANIMATION
    // ========================================
    class StatsCounter {
        constructor() {
            this.counters = document.querySelectorAll('.stat-number');
            this.speed = 200; // Lower is faster
            
            this.init();
        }
        
        init() {
            if (this.counters.length === 0) return;
            
            const observer = new IntersectionObserver(
                (entries) => {
                    entries.forEach(entry => {
                        if (entry.isIntersecting) {
                            this.animateCounter(entry.target);
                            observer.unobserve(entry.target);
                        }
                    });
                },
                { threshold: 0.5 }
            );
            
            this.counters.forEach(counter => observer.observe(counter));
        }
        
        animateCounter(element) {
            const target = element.innerText;
            const num = parseInt(target.replace(/[^0-9]/g, ''));
            const suffix = target.replace(/[0-9]/g, '');
            const increment = num / this.speed;
            let current = 0;
            
            const timer = setInterval(() => {
                current += increment;
                if (current >= num) {
                    element.innerText = num + suffix;
                    clearInterval(timer);
                } else {
                    element.innerText = Math.ceil(current) + suffix;
                }
            }, 1);
        }
    }

    // ========================================
    // BUTTON RIPPLE EFFECT
    // ========================================
    class ButtonRipple {
        constructor() {
            this.init();
        }
        
        init() {
            document.querySelectorAll('.btn').forEach(button => {
                button.addEventListener('click', (e) => this.createRipple(e, button));
            });
        }
        
        createRipple(event, button) {
            const circle = document.createElement('span');
            const diameter = Math.max(button.clientWidth, button.clientHeight);
            const radius = diameter / 2;
            
            const rect = button.getBoundingClientRect();
            circle.style.width = circle.style.height = `${diameter}px`;
            circle.style.left = `${event.clientX - rect.left - radius}px`;
            circle.style.top = `${event.clientY - rect.top - radius}px`;
            circle.classList.add('ripple');
            
            const ripple = button.getElementsByClassName('ripple')[0];
            if (ripple) {
                ripple.remove();
            }
            
            button.appendChild(circle);
        }
    }

    // ========================================
    // PARALLAX EFFECT
    // ========================================
    class ParallaxEffect {
        constructor() {
            this.elements = document.querySelectorAll('.phone-mockup, .blob');
            this.init();
        }
        
        init() {
            if (window.innerWidth < 768) return; // Disable on mobile
            
            window.addEventListener('scroll', () => this.handleScroll());
        }
        
        handleScroll() {
            const scrolled = window.pageYOffset;
            
            this.elements.forEach(element => {
                const speed = element.dataset.speed || 0.5;
                const yPos = -(scrolled * speed);
                element.style.transform = `translateY(${yPos}px)`;
            });
        }
    }

    // ========================================
    // CURSOR GLOW EFFECT
    // ========================================
    class CursorGlow {
        constructor() {
            this.cursor = this.createCursor();
            this.init();
        }
        
        createCursor() {
            const cursor = document.createElement('div');
            cursor.className = 'cursor-glow';
            cursor.style.cssText = `
                position: fixed;
                width: 20px;
                height: 20px;
                border-radius: 50%;
                background: radial-gradient(circle, rgba(102, 126, 234, 0.5), transparent);
                pointer-events: none;
                z-index: 9999;
                transform: translate(-50%, -50%);
                transition: width 0.3s, height 0.3s;
            `;
            document.body.appendChild(cursor);
            return cursor;
        }
        
        init() {
            if (window.innerWidth < 768) return; // Disable on mobile
            
            document.addEventListener('mousemove', (e) => {
                this.cursor.style.left = e.clientX + 'px';
                this.cursor.style.top = e.clientY + 'px';
            });
            
            document.querySelectorAll('a, button').forEach(el => {
                el.addEventListener('mouseenter', () => {
                    this.cursor.style.width = '40px';
                    this.cursor.style.height = '40px';
                });
                
                el.addEventListener('mouseleave', () => {
                    this.cursor.style.width = '20px';
                    this.cursor.style.height = '20px';
                });
            });
        }
    }

    // ========================================
    // LAZY LOADING IMAGES
    // ========================================
    class LazyLoader {
        constructor() {
            this.images = document.querySelectorAll('img[data-src]');
            this.init();
        }
        
        init() {
            if ('IntersectionObserver' in window) {
                const imageObserver = new IntersectionObserver((entries, observer) => {
                    entries.forEach(entry => {
                        if (entry.isIntersecting) {
                            const img = entry.target;
                            img.src = img.dataset.src;
                            img.classList.add('loaded');
                            imageObserver.unobserve(img);
                        }
                    });
                });
                
                this.images.forEach(img => imageObserver.observe(img));
            } else {
                // Fallback for browsers that don't support IntersectionObserver
                this.images.forEach(img => {
                    img.src = img.dataset.src;
                });
            }
        }
    }

    // ========================================
    // INITIALIZE ALL
    // ========================================
    function init() {
        // Wait for DOM to be ready
        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', initializeComponents);
        } else {
            initializeComponents();
        }
    }
    
    function initializeComponents() {
        new ParticlesAnimation('particles-canvas');
        new SmoothScroll();
        new ScrollAnimations();
        new StatsCounter();
        new ButtonRipple();
        new ParallaxEffect();
        new CursorGlow();
        new LazyLoader();
        
        console.log('✨ Explorify Landing Page Initialized');
    }
    
    // Start the application
    init();

})();
