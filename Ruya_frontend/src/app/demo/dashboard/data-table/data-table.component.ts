import { Component, OnInit, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../../services/api.service';

export interface FichierData {
  id: number;
  nomFichier: string;
  typeFichier: string;
  natureFichier?: string;
  codeValeur?: string;
  codEn?: string;
  sens: string;
  montant: number;
  nomber: number;
  createdAt: string;
  updatedAt?: string;
  user: {
    id: number;
    username: string;
    email: string;
  };
}

@Component({
  selector: 'app-data-table',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './data-table.component.html',
  styleUrls: ['./data-table.component.scss']
})
export class DataTableComponent implements OnInit {
  @Input() data: FichierData[] = [];
  
  // Filtres
  dateFilter: string = '';
  statutFilter: string = '';
  typeFilter: string = '';
  searchTerm: string = '';
  
  // Tri
  sortField: string = 'createdAt';
  sortDirection: 'asc' | 'desc' = 'desc';
  
  // Pagination
  currentPage: number = 1;
  itemsPerPage: number = 10;
  totalItems: number = 0;
  totalPages: number = 0;
  
  // Données filtrées et triées
  filteredData: FichierData[] = [];
  displayedData: FichierData[] = [];
  
  // États de chargement
  isLoading = false;
  isDeleting = false;

  // Propriété Math pour le template
  Math = Math;

  constructor(private apiService: ApiService) {}

  ngOnInit() {
    this.loadData();
  }

  loadData() {
    this.isLoading = true;
    this.apiService.getAllFichiers().subscribe({
      next: (data: FichierData[]) => {
        console.log('📋 Données chargées:', data);
        this.data = data;
        this.applyFilters();
        this.isLoading = false;
      },
      error: (error) => {
        console.error('❌ Erreur lors du chargement des données:', error);
        this.data = [];
        this.isLoading = false;
      }
    });
  }

  applyFilters() {
    this.filteredData = this.data.filter(item => {
      const matchesDate = !this.dateFilter || 
        item.createdAt.includes(this.dateFilter);
      
      const matchesStatut = !this.statutFilter || 
        this.getStatutFromCode(item.codeValeur) === this.statutFilter;
      
      const matchesType = !this.typeFilter || 
        item.typeFichier === this.typeFilter;
      
      const matchesSearch = !this.searchTerm || 
        item.nomFichier.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        item.user.username.toLowerCase().includes(this.searchTerm.toLowerCase());
      
      return matchesDate && matchesStatut && matchesType && matchesSearch;
    });
    
    this.sort();
    this.updatePagination();
  }

  sort(field?: string) {
    if (field) {
      if (this.sortField === field) {
        this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
      } else {
        this.sortField = field;
        this.sortDirection = 'asc';
      }
    }
    
    this.filteredData.sort((a, b) => {
      const aValue = this.getFieldValue(a, this.sortField);
      const bValue = this.getFieldValue(b, this.sortField);
      
      if (typeof aValue === 'string' && typeof bValue === 'string') {
        const comparison = aValue.toLowerCase().localeCompare(bValue.toLowerCase());
        return this.sortDirection === 'asc' ? comparison : -comparison;
      }
      
      if (aValue < bValue) return this.sortDirection === 'asc' ? -1 : 1;
      if (aValue > bValue) return this.sortDirection === 'asc' ? 1 : -1;
      return 0;
    });
    
    this.updatePagination();
  }

  getFieldValue(item: FichierData, field: string): any {
    switch (field) {
      case 'nomFichier': return item.nomFichier;
      case 'typeFichier': return item.typeFichier;
      case 'montant': return item.montant;
      case 'createdAt': return item.createdAt;
      case 'user.username': return item.user.username;
      default: return item[field as keyof FichierData];
    }
  }

  changeSort(field: string) {
    this.sort(field);
  }

  getSortIcon(field: string): string {
    if (this.sortField !== field) return 'ti ti-arrows-sort';
    return this.sortDirection === 'asc' ? 'ti ti-sort-ascending' : 'ti ti-sort-descending';
  }

  updatePagination() {
    this.totalItems = this.filteredData.length;
    this.totalPages = Math.ceil(this.totalItems / this.itemsPerPage);
    this.currentPage = 1;
    this.changePage(1);
  }

  changePage(page: number) {
    this.currentPage = page;
    const startIndex = (page - 1) * this.itemsPerPage;
    const endIndex = startIndex + this.itemsPerPage;
    this.displayedData = this.filteredData.slice(startIndex, endIndex);
  }

  getPageNumbers(): number[] {
    const pages: number[] = [];
    
    for (let i = 1; i <= this.totalPages; i++) {
      if (
        i === 1 ||
        i === this.totalPages ||
        (i >= this.currentPage - 2 && i <= this.currentPage + 2)
      ) {
        pages.push(i);
      }
    }
    
    return pages;
  }

  getTypeBadgeClass(type: string): string {
    switch (type) {
      case 'cheque': return 'badge bg-primary';
      case 'virement': return 'badge bg-success';
      case 'prelevement': return 'badge bg-warning';
      case 'effet': return 'badge bg-info';
      default: return 'badge bg-secondary';
    }
  }

  getStatutBadgeClass(statut: string): string {
    switch (statut) {
      case 'REMIS': return 'badge bg-success';
      case 'REJET': return 'badge bg-danger';
      case 'RENDU': return 'badge bg-info';
      case 'EN_ATTENTE': return 'badge bg-warning';
      default: return 'badge bg-secondary';
    }
  }

  getStatutFromCode(code: string | undefined): string {
    if (!code) return 'EN_ATTENTE';
    
    switch (code) {
      case '30': return 'REMIS';
      case '31': return 'REJET';
      case '32': return 'RENDU';
      case '33': return 'EN_ATTENTE';
      default: return 'EN_ATTENTE';
    }
  }

  viewDetails(item: FichierData) {
    console.log('👁️ Détails du fichier:', item);
    // TODO: Implémenter la vue détaillée
  }

  editFichier(item: FichierData) {
    console.log('✏️ Modifier le fichier:', item);
    // TODO: Implémenter la modification
  }

  deleteFichier(item: FichierData) {
    if (confirm(`Êtes-vous sûr de vouloir supprimer le fichier "${item.nomFichier}" ?`)) {
      this.isDeleting = true;
      this.apiService.deleteFichier(item.id).subscribe({
        next: () => {
          console.log('🗑️ Fichier supprimé avec succès');
          this.loadData(); // Recharger les données
          this.isDeleting = false;
        },
        error: (error) => {
          console.error('❌ Erreur lors de la suppression:', error);
          alert('Erreur lors de la suppression du fichier');
          this.isDeleting = false;
        }
      });
    }
  }

  // Méthode pour réinitialiser les filtres
  reinitialiser() {
    console.log('🔄 Réinitialisation des filtres');
    this.dateFilter = '';
    this.statutFilter = '';
    this.typeFilter = '';
    this.searchTerm = '';
    this.currentPage = 0;
    this.applyFilters();
  }
} 