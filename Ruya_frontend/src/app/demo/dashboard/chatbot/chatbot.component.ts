import { Component, OnInit, OnDestroy, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject, takeUntil } from 'rxjs';
import { trigger, state, style, transition, animate } from '@angular/animations';
import { MarkdownPipe } from './markdown.pipe';
import { ChatbotService, ChatMessage, ChatHistory } from './chatbot.service';

@Component({
  selector: 'app-chatbot',
  standalone: true,
  imports: [CommonModule, FormsModule, MarkdownPipe],
  templateUrl: './chatbot.component.html',
  styleUrls: ['./chatbot.component.scss'],
  animations: [
    trigger('messageAnimation', [
      transition(':enter', [
        style({ opacity: 0, transform: 'translateY(10px)' }),
        animate('300ms ease-out', style({ opacity: 1, transform: 'translateY(0)' }))
      ])
    ]),
    trigger('slideDown', [
      transition(':enter', [
        style({ height: 0, opacity: 0 }),
        animate('300ms ease-out', style({ height: '*', opacity: 1 }))
      ]),
      transition(':leave', [
        animate('200ms ease-in', style({ height: 0, opacity: 0 }))
      ])
    ]),
    trigger('fadeIn', [
      transition(':enter', [
        style({ opacity: 0 }),
        animate('200ms ease-out', style({ opacity: 1 }))
      ])
    ])
  ]
})
export class ChatbotComponent implements OnInit, OnDestroy {
  @ViewChild('chatMessages') chatMessagesContainer!: ElementRef;
  @ViewChild('messageInput') messageInputElement!: ElementRef;
  
  messages: ChatMessage[] = [];
  currentMessage: string = '';
  isLoading: boolean = false;
  isExpanded: boolean = false;
  suggestions: string[] = [];
  
  // Nouvelles fonctionnalités
  showHistory: boolean = false;
  chatHistory: ChatHistory[] = [];
  isRecording: boolean = false;
  showQuickActions: boolean = false;
  selectedUserId: string | undefined;
  unreadCount: number = 0;
  
  private destroy$ = new Subject<void>();

  constructor(public chatbotService: ChatbotService) {}

  ngOnInit() {
    // S'abonner aux observables du service
    this.chatbotService.messages$
      .pipe(takeUntil(this.destroy$))
      .subscribe(messages => {
        this.messages = messages;
        this.scrollToBottom();
      });
    
    this.chatbotService.isExpanded$
      .pipe(takeUntil(this.destroy$))
      .subscribe(isExpanded => {
        this.isExpanded = isExpanded;
        if (isExpanded) {
          this.unreadCount = 0;
        }
      });
    
    this.chatbotService.isLoading$
      .pipe(takeUntil(this.destroy$))
      .subscribe(isLoading => this.isLoading = isLoading);
    
    this.chatbotService.suggestions$
      .pipe(takeUntil(this.destroy$))
      .subscribe(suggestions => this.suggestions = suggestions);
    
    // Charger les suggestions
    this.chatbotService.loadSuggestions();
    
    // Charger l'historique
    this.loadChatHistory();
  }
  
  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
  
  /**
   * Charge l'historique des conversations
   */
  loadChatHistory(): void {
    this.chatHistory = this.chatbotService.getChatHistory();
  }

  /**
   * Envoie un message au chatbot
   */
  sendMessage(): void {
    if (!this.currentMessage.trim() || this.isLoading) return;

    // Créer et ajouter le message utilisateur
    const userMessage: ChatMessage = {
      id: this.generateMessageId(),
      text: this.currentMessage,
      isUser: true,
      timestamp: new Date()
    };
    
    this.chatbotService.addMessage(userMessage);

    const question = this.currentMessage;
    this.currentMessage = '';
    this.chatbotService.setLoading(true);

    // Envoyer la question au backend
    this.chatbotService.sendQuestion(question, this.selectedUserId).subscribe({
      next: (response) => {
        const botMessage: ChatMessage = {
          id: this.generateMessageId(),
          text: response.response,
          isUser: false,
          timestamp: new Date(),
          type: response.type as any,
          data: response.data
        };
        
        this.chatbotService.addMessage(botMessage);
        this.chatbotService.setLoading(false);
        
        // Si le chatbot n'est pas ouvert, incrémenter le compteur
        if (!this.isExpanded) {
          this.unreadCount++;
        }
      },
      error: (error) => {
        console.error('Erreur chatbot:', error);
        const errorMessage: ChatMessage = {
          id: this.generateMessageId(),
          text: 'Désolé, une erreur s\'est produite. Veuillez réessayer.',
          isUser: false,
          timestamp: new Date(),
          type: 'error'
        };
        
        this.chatbotService.addMessage(errorMessage);
        this.chatbotService.setLoading(false);
      }
    });
  }
  
  /**
   * Génère un ID de message unique
   */
  private generateMessageId(): string {
    return `msg_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  }

  /**
   * Utilise une suggestion
   */
  useSuggestion(suggestion: string): void {
    this.currentMessage = suggestion;
    this.sendMessage();
  }

  /**
   * Toggle le chatbot
   */
  toggleChat(): void {
    this.chatbotService.toggleExpanded();
  }

  /**
   * Efface la conversation
   */
  clearChat(): void {
    if (confirm('Êtes-vous sûr de vouloir effacer cette conversation ?')) {
      this.chatbotService.clearChat();
    }
  }
  
  /**
   * Exporte la conversation
   */
  exportConversation(): void {
    this.chatbotService.downloadConversation();
  }
  
  /**
   * Affiche/masque l'historique
   */
  toggleHistory(): void {
    this.showHistory = !this.showHistory;
    if (this.showHistory) {
      this.loadChatHistory();
    }
  }
  
  /**
   * Charge une session depuis l'historique
   */
  loadSessionFromHistory(sessionId: string): void {
    this.chatbotService.loadSession(sessionId);
    this.showHistory = false;
  }
  
  /**
   * Supprime l'historique complet
   */
  clearHistory(): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer tout l\'historique ?')) {
      this.chatbotService.clearHistory();
      this.chatHistory = [];
      this.showHistory = false;
    }
  }
  
  /**
   * Toggle les actions rapides
   */
  toggleQuickActions(): void {
    this.showQuickActions = !this.showQuickActions;
  }
  
  /**
   * Exécute une action rapide
   */
  executeQuickAction(action: string): void {
    this.currentMessage = action;
    this.sendMessage();
    this.showQuickActions = false;
  }
  
  /**
   * Formate la date d'une session
   */
  formatSessionDate(date: Date): string {
    const sessionDate = new Date(date);
    const now = new Date();
    const diffInHours = (now.getTime() - sessionDate.getTime()) / (1000 * 60 * 60);
    
    if (diffInHours < 1) {
      return 'Il y a quelques minutes';
    } else if (diffInHours < 24) {
      return `Il y a ${Math.floor(diffInHours)} heures`;
    } else {
      return sessionDate.toLocaleDateString('fr-FR');
    }
  }

  /**
   * Scroll vers le bas
   */
  scrollToBottom(): void {
    setTimeout(() => {
      const chatContainer = document.querySelector('.chat-messages');
      if (chatContainer) {
        chatContainer.scrollTop = chatContainer.scrollHeight;
      }
    }, 100);
  }

  /**
   * Gestion de la touche Entrée
   */
  onKeyPress(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }
  
  /**
   * Copie un message
   */
  copyMessage(message: ChatMessage): void {
    navigator.clipboard.writeText(message.text).then(() => {
      // Optionnel: afficher une notification de succès
      console.log('Message copié !');
    });
  }
  
  /**
   * Obtient les statistiques d'utilisation
   */
  getUsageStats(): string {
    const stats = this.chatbotService.getUsageStatistics();
    return `${stats.totalSessions} sessions • ${stats.totalMessages} messages • Moyenne: ${stats.averageMessagesPerSession} msgs/session`;
  }
}
