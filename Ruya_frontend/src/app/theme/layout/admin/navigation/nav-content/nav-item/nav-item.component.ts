// Angular import
import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';

// Project import
import { NavigationItem } from '../../navigation';
import { AjouterFichierService } from 'src/app/demo/ajouter-fichier/ajouter-fichier.service';

@Component({
  selector: 'app-nav-item',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './nav-item.component.html',
  styleUrl: './nav-item.component.scss'
})
export class NavItemComponent {
  // public props
  @Input() item!: NavigationItem;

  constructor(
    private ajouterFichierService: AjouterFichierService,
    private router: Router
  ) {}

  // Méthode pour vérifier si l'élément est actif
  isActive(): boolean {
    // Vérifier si l'URL actuelle correspond à l'URL de l'élément
    const currentUrl = this.router.url;
    const itemUrl = this.item.url;
    
    // Pour les éléments sans URL, retourner false
    if (!itemUrl) return false;
    
    // Comparaison exacte des URLs
    return currentUrl === itemUrl;
  }

  // Méthode pour gérer le clic sur l'item
  onItemClick(event: MouseEvent) {
    // Si c'est l'item "Ajouter un fichier", ouvrir le modal
    if (this.item.id === 'ajouter-fichier') {
      event.preventDefault();
      this.ajouterFichierService.openModal();
      return;
    }

    // Pour tous les autres éléments, gérer l'état actif
    this.handleActiveState(event);
  }

  // Méthode pour gérer l'état actif
  private handleActiveState(event: MouseEvent) {
    // Supprimer l'état actif de tous les éléments
    this.clearAllActiveStates();
    
    // Activer l'élément cliqué
    const clickedElement = event.currentTarget as HTMLElement;
    const navItem = clickedElement.closest('.nav-item');
    
    if (navItem) {
      navItem.classList.add('active');
    }
    
    // Fermer tous les sous-menus ouverts
    this.closeAllSubmenus();
  }

  // Méthode pour supprimer tous les états actifs
  private clearAllActiveStates() {
    // Supprimer 'active' de tous les éléments de navigation
    const allNavItems = document.querySelectorAll('.nav-item');
    allNavItems.forEach(item => {
      item.classList.remove('active');
    });
    
    // Supprimer 'active' et 'coded-trigger' de tous les menus collapsibles
    const allCollapsibleMenus = document.querySelectorAll('.coded-hasmenu');
    allCollapsibleMenus.forEach(menu => {
      menu.classList.remove('active');
      menu.classList.remove('coded-trigger');
    });
  }

  // Méthode pour fermer tous les sous-menus
  private closeAllSubmenus() {
    const allSubmenus = document.querySelectorAll('.coded-submenu');
    allSubmenus.forEach(submenu => {
      const parent = submenu.closest('.coded-hasmenu');
      if (parent) {
        parent.classList.remove('coded-trigger');
        parent.classList.remove('active');
      }
    });
  }
}
