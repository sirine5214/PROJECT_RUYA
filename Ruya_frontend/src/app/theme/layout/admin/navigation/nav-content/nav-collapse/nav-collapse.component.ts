// Angular import
import { Component, Input, OnInit, inject } from '@angular/core';
import { CommonModule, Location } from '@angular/common';
import { RouterModule } from '@angular/router';

// project import
import { NavigationItem } from '../../navigation';

import { NavItemComponent } from '../nav-item/nav-item.component';

@Component({
  selector: 'app-nav-collapse',
  standalone: true,
  imports: [CommonModule, RouterModule, NavItemComponent],
  templateUrl: './nav-collapse.component.html',
  styleUrl: './nav-collapse.component.scss'
})
export class NavCollapseComponent implements OnInit {
  private location = inject(Location);

  // public props
  @Input() item!: NavigationItem;
  windowWidth = window.innerWidth;
  current_url = ''; // Add current URL property

  ngOnInit() {
    this.current_url = this.location.path();

    // eslint-disable-next-line
    //@ts-ignore
    const baseHref = this.location['_baseHref'] || ''; // Use baseHref if necessary
    this.current_url = baseHref + this.current_url;

    // Timeout to allow DOM to fully render before checking for the links
    setTimeout(() => {
      const links = document.querySelectorAll('a.nav-link') as NodeListOf<HTMLAnchorElement>;
      links.forEach((link: HTMLAnchorElement) => {
        if (link.getAttribute('href') === this.current_url) {
          let parent = link.parentElement;
          while (parent && parent.classList) {
            if (parent.classList.contains('coded-hasmenu')) {
              parent.classList.add('coded-trigger');
              parent.classList.add('active');
            }
            parent = parent.parentElement;
          }
        }
      });
    }, 0);
  }

  // Method to handle the collapse of the navigation menu
  navCollapse(e: MouseEvent) {
    e.preventDefault();
    e.stopPropagation();
    
    let parent = e.target as HTMLElement;

    if (parent?.tagName === 'SPAN') {
      parent = parent.parentElement!;
    }

    parent = (parent as HTMLElement).parentElement!;

    // Vérifier si le menu cliqué est déjà ouvert
    const isCurrentlyOpen = parent.classList.contains('coded-trigger');

    // Fermer tous les autres menus d'abord
    this.closeOtherMenus(parent);

    // Basculer l'état du menu cliqué
    if (isCurrentlyOpen) {
      // Si le menu est déjà ouvert, le fermer
      parent.classList.remove('coded-trigger');
      parent.classList.remove('active');
    } else {
      // Si le menu est fermé, l'ouvrir
      parent.classList.add('coded-trigger');
      parent.classList.add('active');
    }

    // Supprimer l'état actif des éléments simples
    this.clearSimpleItemStates();
  }

  // Méthode pour fermer tous les autres menus
  private closeOtherMenus(currentMenu: HTMLElement) {
    const sections = document.querySelectorAll('.coded-hasmenu');
    sections.forEach(section => {
      if (section !== currentMenu) {
        section.classList.remove('coded-trigger');
        section.classList.remove('active');
      }
    });
  }

  // Méthode pour supprimer l'état actif des éléments simples
  private clearSimpleItemStates() {
    const allNavItems = document.querySelectorAll('.nav-item');
    allNavItems.forEach(item => {
      // Ne pas supprimer l'état actif si l'item est dans un sous-menu ouvert
      const parentMenu = item.closest('.coded-hasmenu');
      if (!parentMenu || !parentMenu.classList.contains('coded-trigger')) {
        item.classList.remove('active');
      }
    });
  }
}
