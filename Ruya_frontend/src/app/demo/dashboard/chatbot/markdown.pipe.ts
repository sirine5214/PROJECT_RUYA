import { Pipe, PipeTransform } from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';

@Pipe({
  name: 'markdown',
  standalone: true
})
export class MarkdownPipe implements PipeTransform {
  
  constructor(private sanitizer: DomSanitizer) {}

  transform(value: string): SafeHtml {
    if (!value) return '';

    let result = value;
    
    // Headers (### Header, ## Header, # Header)
    result = result.replace(/^### (.*$)/gim, '<h3>$1</h3>');
    result = result.replace(/^## (.*$)/gim, '<h2>$1</h2>');
    result = result.replace(/^# (.*$)/gim, '<h1>$1</h1>');
    
    // Bold text **text** or __text__
    result = result.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');
    result = result.replace(/__(.*?)__/g, '<strong>$1</strong>');
    
    // Italic text *text* or _text_
    result = result.replace(/\*([^*]+)\*/g, '<em>$1</em>');
    result = result.replace(/_([^_]+)_/g, '<em>$1</em>');
    
    // Strikethrough ~~text~~
    result = result.replace(/~~(.*?)~~/g, '<del>$1</del>');
    
    // Inline code `code`
    result = result.replace(/`([^`]+)`/g, '<code class="inline-code">$1</code>');
    
    // Links [text](url)
    result = result.replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank" rel="noopener noreferrer">$1</a>');
    
    // Unordered lists (• item)
    result = result.replace(/^[•\-\*] (.*)$/gim, '<li>$1</li>');
    result = result.replace(/(<li>.*<\/li>)/s, '<ul>$1</ul>');
    
    // Tables (simple format)
    // | Header 1 | Header 2 |
    // | Value 1  | Value 2  |
    const tableRegex = /\|(.+)\|/g;
    if (tableRegex.test(result)) {
      result = result.replace(/\|(.+)\|/g, (match, content) => {
        const cells = content.split('|').map((cell: string) => cell.trim());
        const cellsHtml = cells.map((cell: string) => `<td>${cell}</td>`).join('');
        return `<tr>${cellsHtml}</tr>`;
      });
      result = result.replace(/(<tr>.*<\/tr>)/s, '<table class="markdown-table">$1</table>');
    }
    
    // Blockquotes > text
    result = result.replace(/^&gt; (.*)$/gim, '<blockquote>$1</blockquote>');
    result = result.replace(/^> (.*)$/gim, '<blockquote>$1</blockquote>');
    
    // Horizontal rules ---
    result = result.replace(/^---$/gim, '<hr>');
    
    // Line breaks (double space at end of line or \n)
    result = result.replace(/  \n/g, '<br>');
    result = result.replace(/\n\n/g, '<br><br>');
    result = result.replace(/\n/g, '<br>');
    
    // Emojis et caractères spéciaux
    const emojiMap: { [key: string]: string } = {
      ':smile:': '😊',
      ':check:': '✅',
      ':cross:': '❌',
      ':info:': 'ℹ️',
      ':warning:': '⚠️',
      ':money:': '💰',
      ':chart:': '📊',
      ':file:': '📁',
      ':calendar:': '📅',
      ':clock:': '🕐',
      ':robot:': '🤖',
      ':rocket:': '🚀',
      ':bulb:': '💡',
      ':star:': '⭐',
      ':fire:': '🔥',
      ':heart:': '❤️'
    };
    
    Object.keys(emojiMap).forEach(key => {
      const regex = new RegExp(key.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'), 'g');
      result = result.replace(regex, `<span class="emoji">${emojiMap[key]}</span>`);
    });
    
    // Emoji natifs
    result = result.replace(
      /([\u{1F300}-\u{1F9FF}]|[\u{2600}-\u{26FF}]|[\u{2700}-\u{27BF}])/gu,
      '<span class="emoji">$1</span>'
    );
    
    // Highlight important text ==text==
    result = result.replace(/==(.*?)==/g, '<mark>$1</mark>');
    
    // Subscript ~text~
    result = result.replace(/~(.*?)~/g, '<sub>$1</sub>');
    
    // Superscript ^text^
    result = result.replace(/\^(.*?)\^/g, '<sup>$1</sup>');
    
    // Ajouter des classes pour le styling
    result = result.replace(/<strong>/g, '<strong class="markdown-bold">');
    result = result.replace(/<em>/g, '<em class="markdown-italic">');
    result = result.replace(/<a /g, '<a class="markdown-link" ');
    result = result.replace(/<ul>/g, '<ul class="markdown-list">');
    result = result.replace(/<blockquote>/g, '<blockquote class="markdown-quote">');
    
    return this.sanitizer.sanitize(1, result) || '';
  }
}
