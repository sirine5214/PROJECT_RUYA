import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';

export interface ChatMessage {
  id: string;
  text: string;
  isUser: boolean;
  timestamp: Date;
  type?: 'error' | 'success' | 'info' | 'warning';
  data?: any;
}

export interface ChatResponse {
  response: string;
  type: string;
  data?: any;
}

export interface ChatHistory {
  sessionId: string;
  messages: ChatMessage[];
  startTime: Date;
  endTime?: Date;
}

@Injectable({
  providedIn: 'root'
})
export class ChatbotService {
  private apiUrl = 'http://localhost:8081/api/chatbot';
  
  // Observables pour l'état du chatbot
  private messagesSubject = new BehaviorSubject<ChatMessage[]>([]);
  public messages$ = this.messagesSubject.asObservable();
  
  private isExpandedSubject = new BehaviorSubject<boolean>(false);
  public isExpanded$ = this.isExpandedSubject.asObservable();
  
  private isLoadingSubject = new BehaviorSubject<boolean>(false);
  public isLoading$ = this.isLoadingSubject.asObservable();
  
  private suggestionsSubject = new BehaviorSubject<string[]>([]);
  public suggestions$ = this.suggestionsSubject.asObservable();
  
  // Historique des conversations
  private chatHistoryKey = 'ruya_chat_history';
  private currentSessionId: string;
  
  constructor(private http: HttpClient) {
    this.currentSessionId = this.generateSessionId();
    this.loadPreviousSession();
  }
  
  /**
   * Génère un ID de session unique
   */
  private generateSessionId(): string {
    return `session_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  }
  
  /**
   * Charge la session précédente si elle existe
   */
  private loadPreviousSession(): void {
    try {
      const savedHistory = localStorage.getItem(this.chatHistoryKey);
      if (savedHistory) {
        const history: ChatHistory[] = JSON.parse(savedHistory);
        const lastSession = history[history.length - 1];
        
        // Si la dernière session date de moins de 24h, la restaurer
        if (lastSession && this.isSessionRecent(lastSession.startTime)) {
          this.messagesSubject.next(lastSession.messages);
          this.currentSessionId = lastSession.sessionId;
        } else {
          this.initializeChat();
        }
      } else {
        this.initializeChat();
      }
    } catch (error) {
      console.error('Erreur lors du chargement de la session:', error);
      this.initializeChat();
    }
  }
  
  /**
   * Vérifie si une session est récente (moins de 24h)
   */
  private isSessionRecent(startTime: Date): boolean {
    const now = new Date();
    const sessionDate = new Date(startTime);
    const diffInHours = (now.getTime() - sessionDate.getTime()) / (1000 * 60 * 60);
    return diffInHours < 24;
  }
  
  /**
   * Initialise le chat avec un message de bienvenue
   */
  initializeChat(): void {
    const welcomeMessage: ChatMessage = {
      id: this.generateMessageId(),
      text: '🤖 Bonjour! Je suis votre assistant RU\'ya intelligent.\n\n' +
            'Je peux vous aider avec:\n' +
            '• 📊 Statistiques et analyses en temps réel\n' +
            '• 📁 Gestion des fichiers (chèques, virements, effets)\n' +
            '• 💰 Analyses financières détaillées\n' +
            '• ⏱️ Temps de traitement et performances\n' +
            '• 📈 Tendances et prévisions\n\n' +
            'Comment puis-je vous aider aujourd\'hui?',
      isUser: false,
      timestamp: new Date(),
      type: 'info'
    };
    
    this.messagesSubject.next([welcomeMessage]);
    this.saveCurrentSession();
  }
  
  /**
   * Génère un ID de message unique
   */
  private generateMessageId(): string {
    return `msg_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  }
  
  /**
   * Ajoute un message à la conversation
   */
  addMessage(message: ChatMessage): void {
    const currentMessages = this.messagesSubject.value;
    this.messagesSubject.next([...currentMessages, message]);
    this.saveCurrentSession();
  }
  
  /**
   * Envoie une question au backend
   */
  sendQuestion(question: string, userId?: string): Observable<ChatResponse> {
    const requestBody = userId ? { question, userId } : { question };
    return this.http.post<ChatResponse>(`${this.apiUrl}/ask`, requestBody);
  }
  
  /**
   * Charge les suggestions depuis le backend
   */
  loadSuggestions(): void {
    this.http.get<string[]>(`${this.apiUrl}/suggestions`).subscribe({
      next: (suggestions) => {
        this.suggestionsSubject.next(suggestions);
      },
      error: (error) => {
        console.error('Erreur lors du chargement des suggestions:', error);
      }
    });
  }
  
  /**
   * Toggle l'état d'expansion du chatbot
   */
  toggleExpanded(): void {
    this.isExpandedSubject.next(!this.isExpandedSubject.value);
  }
  
  /**
   * Définit l'état d'expansion
   */
  setExpanded(expanded: boolean): void {
    this.isExpandedSubject.next(expanded);
  }
  
  /**
   * Définit l'état de chargement
   */
  setLoading(loading: boolean): void {
    this.isLoadingSubject.next(loading);
  }
  
  /**
   * Efface la conversation actuelle
   */
  clearChat(): void {
    this.currentSessionId = this.generateSessionId();
    this.initializeChat();
  }
  
  /**
   * Sauvegarde la session actuelle dans l'historique
   */
  private saveCurrentSession(): void {
    try {
      const currentSession: ChatHistory = {
        sessionId: this.currentSessionId,
        messages: this.messagesSubject.value,
        startTime: new Date(this.currentSessionId.split('_')[1]),
        endTime: new Date()
      };
      
      const savedHistory = localStorage.getItem(this.chatHistoryKey);
      let history: ChatHistory[] = savedHistory ? JSON.parse(savedHistory) : [];
      
      // Trouve et met à jour la session actuelle ou l'ajoute
      const sessionIndex = history.findIndex(s => s.sessionId === this.currentSessionId);
      if (sessionIndex >= 0) {
        history[sessionIndex] = currentSession;
      } else {
        history.push(currentSession);
      }
      
      // Limite l'historique aux 10 dernières sessions
      if (history.length > 10) {
        history = history.slice(-10);
      }
      
      localStorage.setItem(this.chatHistoryKey, JSON.stringify(history));
    } catch (error) {
      console.error('Erreur lors de la sauvegarde de la session:', error);
    }
  }
  
  /**
   * Récupère l'historique complet des conversations
   */
  getChatHistory(): ChatHistory[] {
    try {
      const savedHistory = localStorage.getItem(this.chatHistoryKey);
      return savedHistory ? JSON.parse(savedHistory) : [];
    } catch (error) {
      console.error('Erreur lors de la récupération de l\'historique:', error);
      return [];
    }
  }
  
  /**
   * Charge une session spécifique
   */
  loadSession(sessionId: string): void {
    const history = this.getChatHistory();
    const session = history.find(s => s.sessionId === sessionId);
    
    if (session) {
      this.currentSessionId = sessionId;
      this.messagesSubject.next(session.messages);
    }
  }
  
  /**
   * Supprime l'historique complet
   */
  clearHistory(): void {
    localStorage.removeItem(this.chatHistoryKey);
    this.currentSessionId = this.generateSessionId();
    this.initializeChat();
  }
  
  /**
   * Exporte la conversation actuelle en texte
   */
  exportConversation(): string {
    const messages = this.messagesSubject.value;
    let text = `Conversation RU'ya - ${new Date().toLocaleString()}\n`;
    text += '='.repeat(60) + '\n\n';
    
    messages.forEach(msg => {
      const sender = msg.isUser ? 'Vous' : 'Assistant RU\'ya';
      const time = new Date(msg.timestamp).toLocaleTimeString();
      text += `[${time}] ${sender}:\n${msg.text}\n\n`;
    });
    
    return text;
  }
  
  /**
   * Télécharge la conversation actuelle
   */
  downloadConversation(): void {
    const text = this.exportConversation();
    const blob = new Blob([text], { type: 'text/plain' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `ruya_conversation_${Date.now()}.txt`;
    link.click();
    window.URL.revokeObjectURL(url);
  }
  
  /**
   * Récupère les statistiques d'utilisation du chatbot
   */
  getUsageStatistics(): { totalSessions: number; totalMessages: number; averageMessagesPerSession: number } {
    const history = this.getChatHistory();
    const totalSessions = history.length;
    const totalMessages = history.reduce((sum, session) => sum + session.messages.length, 0);
    const averageMessagesPerSession = totalSessions > 0 ? totalMessages / totalSessions : 0;
    
    return {
      totalSessions,
      totalMessages,
      averageMessagesPerSession: Math.round(averageMessagesPerSession * 10) / 10
    };
  }
  
  /**
   * Obtient les messages actuels
   */
  getMessages(): ChatMessage[] {
    return this.messagesSubject.value;
  }
  
  /**
   * Obtient l'état d'expansion actuel
   */
  getIsExpanded(): boolean {
    return this.isExpandedSubject.value;
  }
  
  /**
   * Obtient l'état de chargement actuel
   */
  getIsLoading(): boolean {
    return this.isLoadingSubject.value;
  }
  
  /**
   * Obtient les suggestions actuelles
   */
  getSuggestions(): string[] {
    return this.suggestionsSubject.value;
  }
}
