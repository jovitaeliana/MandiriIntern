document.addEventListener('DOMContentLoaded', function() {
    // Initialize syntax highlighting
    hljs.highlightAll();
    
    // Sticky navbar functionality
    const navbar = document.getElementById('navbar');
    const navbarOffset = navbar.offsetTop;
    
    // Smooth scrolling for navigation links
    const navLinks = document.querySelectorAll('nav a');
    navLinks.forEach(link => {
        link.addEventListener('click', e => {
            e.preventDefault();
            const targetId = link.getAttribute('href');
            const targetElement = document.querySelector(targetId);
            
            window.scrollTo({
                top: targetElement.offsetTop - navbar.offsetHeight,
                behavior: 'smooth'
            });
            
            // Update active link
            navLinks.forEach(link => link.classList.remove('active'));
            link.classList.add('active');
        });
    });
    
    // Handle scroll events
    window.addEventListener('scroll', () => {
        // Stick navbar to top when scrolling
        if (window.pageYOffset >= navbarOffset) {
            navbar.classList.add('sticky');
        } else {
            navbar.classList.remove('sticky');
        }
        
        // Update active navigation link based on scroll position
        const scrollPosition = window.scrollY;
        
        document.querySelectorAll('section').forEach(section => {
            const sectionTop = section.offsetTop - navbar.offsetHeight - 10;
            const sectionBottom = sectionTop + section.offsetHeight;
            
            if (scrollPosition >= sectionTop && scrollPosition < sectionBottom) {
                const currentId = section.getAttribute('id');
                navLinks.forEach(link => {
                    link.classList.remove('active');
                    if (link.getAttribute('href') === `#${currentId}`) {
                        link.classList.add('active');
                    }
                });
            }
        });
    });
    
    // Add active class styles to navigation
    const navbarStyle = document.createElement('style');
    navbarStyle.innerHTML = `
        nav a.active {
            color: var(--secondary-color);
        }
        nav a.active::after {
            width: 80%;
        }
        .sticky {
            position: fixed;
            top: 0;
            width: 100%;
        }
        .sticky + main {
            padding-top: 60px;
        }
    `;
    document.head.appendChild(navbarStyle);
    
    // Add loading animation for code blocks
    const codeBlocks = document.querySelectorAll('pre code');
    codeBlocks.forEach(block => {
        block.style.opacity = '0';
        block.style.transition = 'opacity 0.5s ease-in-out';
        
        // Create observer for lazy loading code blocks
        const observer = new IntersectionObserver(entries => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    setTimeout(() => {
                        entry.target.style.opacity = '1';
                    }, 300);
                    observer.unobserve(entry.target);
                }
            });
        }, { threshold: 0.1 });
        
        observer.observe(block);
    });
    
    // Add hover effect to feature cards
    const featureCards = document.querySelectorAll('.feature-card');
    featureCards.forEach(card => {
        card.addEventListener('mouseenter', () => {
            card.style.transform = 'translateY(-10px)';
        });
        
        card.addEventListener('mouseleave', () => {
            card.style.transform = 'translateY(0)';
        });
    });
    
    console.log('Mandiri News Portfolio initialized successfully!');
});


// Add image gallery functionality
document.addEventListener('DOMContentLoaded', function() {
    // Make screenshots clickable for larger view
    const screenshots = document.querySelectorAll('.feature-image');
    screenshots.forEach(image => {
        image.addEventListener('click', function() {
            // Create modal overlay
            const overlay = document.createElement('div');
            overlay.className = 'image-overlay';
            
            // Create large image
            const largeImg = document.createElement('img');
            largeImg.src = this.src;
            largeImg.className = 'large-image';
            
            // Create close button
            const closeBtn = document.createElement('span');
            closeBtn.innerHTML = '&times;';
            closeBtn.className = 'close-button';
            
            overlay.appendChild(closeBtn);
            overlay.appendChild(largeImg);
            document.body.appendChild(overlay);
            
            // Prevent scrolling when modal is open
            document.body.style.overflow = 'hidden';
            
            // Close modal on click
            overlay.addEventListener('click', function() {
                document.body.removeChild(overlay);
                document.body.style.overflow = 'auto';
            });
        });
    });
    
    // Add CSS for the modal overlay
    const modalStyle = document.createElement('style');
    modalStyle.innerHTML = `
        .image-overlay {
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background-color: rgba(0, 0, 0, 0.9);
            display: flex;
            justify-content: center;
            align-items: center;
            z-index: 2000;
            cursor: pointer;
        }
        
        .large-image {
            max-width: 90%;
            max-height: 90%;
            object-fit: contain;
        }
        
        .close-button {
            position: absolute;
            top: 20px;
            right: 30px;
            color: white;
            font-size: 40px;
            font-weight: bold;
            cursor: pointer;
        }
    `;
    document.head.appendChild(modalStyle);
});