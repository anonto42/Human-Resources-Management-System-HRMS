package com.hrmf.hrms_backend.controller.basic;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/home")
public class HomeController {

    @GetMapping("/status")
    public String home() {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "  <meta charset=\"UTF-8\"/>\n" +
                "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"/>\n" +
                "  <title>Hypernova Server — All Systems Go</title>\n" +
                "  <style>\n" +
                "    @import url('https://fonts.googleapis.com/css2?family=Orbitron:wght@700&family=Rajdhani:wght@500&display=swap');\n" +
                "    * { margin:0; padding:0; box-sizing:border-box; }\n" +
                "    body {\n" +
                "      height:100vh;\n" +
                "      overflow:hidden;\n" +
                "      font-family:'Orbitron',sans-serif;\n" +
                "      display:flex; align-items:center; justify-content:center;\n" +
                "      background: linear-gradient(135deg, #0b0f20, #05070c);\n" +
                "      position:relative;\n" +
                "    }\n" +
                "    .particles {\n" +
                "      position:absolute; width:100%; height:100%; background:#05070c;\n" +
                "      background: radial-gradient(circle at center, #001022 0%, #000 80%);\n" +
                "      z-index:0; overflow:hidden;\n" +
                "    }\n" +
                "    .particle {\n" +
                "      position:absolute; background:#00f6ff;\n" +
                "      opacity:0.6; border-radius:50%;\n" +
                "      animation: drift 6s ease-in-out infinite;\n" +
                "    }\n" +
                "    @keyframes drift {\n" +
                "      0% { transform: translate(0,0) scale(0.5); }\n" +
                "      50% { transform: translate(var(--dx), var(--dy)) scale(1); }\n" +
                "      100% { transform: translate(0,0) scale(0.5); }\n" +
                "    }\n" +
                "    .main {\n" +
                "      position:relative; z-index:2;\n" +
                "      background: rgba(0,0,0,0.5);\n" +
                "      padding:3rem 4rem;\n" +
                "      border:2px solid #00f6ff; border-radius:20px;\n" +
                "      box-shadow:\n" +
                "        0 0 20px #00f6ff,\n" +
                "        0 0 40px #ff00ff,\n" +
                "        0 0 80px #00f6ff;\n" +
                "      animation: pulseBox 5s ease-in-out infinite;\n" +
                "      text-align:center;\n" +
                "    }\n" +
                "    @keyframes pulseBox {\n" +
                "      0%,100% { transform:scale(1); box-shadow:0 0 20px #00f6ff,0 0 40px #ff00ff; }\n" +
                "      50% { transform:scale(1.05); box-shadow:0 0 60px #00f6ff,0 0 80px #ff00ff; }\n" +
                "    }\n" +
                "    .icon {\n" +
                "      font-size:6rem; color:#00f6ff;\n" +
                "      text-shadow:0 0 30px #00f6ff,0 0 60px #ff00ff;\n" +
                "      animation: rotateGlow 7s linear infinite;\n" +
                "    }\n" +
                "    @keyframes rotateGlow { 0%{transform:rotate(0deg);}100%{transform:rotate(360deg);} }\n" +
                "    h1 {\n" +
                "      margin:1rem 0; font-size:3rem; color:#fff;\n" +
                "      text-shadow:0 0 20px #00f6ff,0 0 40px #ff00ff;\n" +
                "      animation: flicker 4s infinite;\n" +
                "    }\n" +
                "    @keyframes flicker {\n" +
                "      0%,100% { opacity:1; }\n" +
                "      40%,60% { opacity:0.7; }\n" +
                "    }\n" +
                "    p {\n" +
                "      color:#ccc; font-family:'Rajdhani',sans-serif;\n" +
                "      font-size:1.3rem;\n" +
                "    }\n" +
                "  </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "  <div class=\"particles\" id=\"particles\"></div>\n" +
                "  <div class=\"main\">\n" +
                "    <div class=\"icon\">\uD83D\uDE80</div>\n" +
                "    <h1>Hypernova Server Online</h1>\n" +
                "    <p>System fully operational. All channels green, ready for hyperspace requests!</p>\n" +
                "  </div>\n" +
                "  <script>\n" +
                "    const container = document.getElementById('particles');\n" +
                "    for(let i=0;i<15;i++){\n" +
                "      const p=document.createElement('div');\n" +
                "      p.classList.add('particle');\n" +
                "      const size=Math.random()*8+4;\n" +
                "      p.style.width=\\`\\${size}px\\`;\n" +
                "      p.style.height=\\`\\${size}px\\`;\n" +
                "      p.style.top=\\`\\${Math.random()*100}%\\`;\n" +
                "      p.style.left=\\`\\${Math.random()*100}%\\`;\n" +
                "      p.style.setProperty('--dx', (Math.random()*200-100)+'px');\n" +
                "      p.style.setProperty('--dy', (Math.random()*200-100)+'px');\n" +
                "      p.style.animationDelay=\\`\\${Math.random()*3}s\\`;\n" +
                "      container.appendChild(p);\n" +
                "    }\n" +
                "  </script>\n" +
                "</body>\n" +
                "</html>";
    }
}
