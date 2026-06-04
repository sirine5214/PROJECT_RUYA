// Angular import
import { Component, OnInit, output, inject } from '@angular/core';
import { CommonModule, Location } from '@angular/common';
import { RouterModule } from '@angular/router';

//theme version
import { environment } from 'src/environments/environment';

// project import
import { NavigationItem, NavigationItems } from '../navigation';

import { NavCollapseComponent } from './nav-collapse/nav-collapse.component';
import { NavGroupComponent } from './nav-group/nav-group.component';
import { NavItemComponent } from './nav-item/nav-item.component';

// NgScrollbarModule
import { SharedModule } from 'src/app/theme/shared/shared.module';

@Component({
  selector: 'app-nav-content',
  standalone: true,
  imports: [CommonModule, RouterModule, NavCollapseComponent, NavGroupComponent, NavItemComponent, SharedModule],
  templateUrl: './nav-content.component.html',
  styleUrl: './nav-content.component.scss'
})
export class NavContentComponent implements OnInit {
  private location = inject(Location);

  // public props
  NavCollapsedMob = output();
  SubmenuCollapse = output();

  // version
  title = 'Demo application for version numbering';
  currentApplicationVersion = environment.appVersion;

  navigations!: NavigationItem[];
  windowWidth: number;

  // Constructor
  constructor() {
    this.windowWidth = window.innerWidth;
  }

  // Life cycle events
  ngOnInit() {
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    const userRoles = Array.isArray(user.role) ? user.role : [user.role];

    this.navigations = this.filterByRole(NavigationItems, userRoles);
  
    if (this.windowWidth < 1025) {
      setTimeout(() => {
        (document.querySelector('.coded-navbar') as HTMLDivElement).classList.add('menupos-static');
      }, 500);
    }

    // Gérer l'état actif par défaut
    this.setDefaultActiveState();
  }

  fireOutClick() {
    let current_url = this.location.path();
    // eslint-disable-next-line
    // @ts-ignore
    if (this.location['_baseHref']) {
      // eslint-disable-next-line
      // @ts-ignore
      current_url = this.location['_baseHref'] + this.location.path();
    }
    const link = "a.nav-link[ href='" + current_url + "' ]";
    const ele = document.querySelector(link);
    if (ele !== null && ele !== undefined) {
      const parent = ele.parentElement;
      const up_parent = parent?.parentElement?.parentElement;
      const last_parent = up_parent?.parentElement;
      if (parent?.classList.contains('coded-hasmenu')) {
        parent.classList.add('coded-trigger');
        parent.classList.add('active');
      } else if (up_parent?.classList.contains('coded-hasmenu')) {
        up_parent.classList.add('coded-trigger');
        up_parent.classList.add('active');
      } else if (last_parent?.classList.contains('coded-hasmenu')) {
        last_parent.classList.add('coded-trigger');
        last_parent.classList.add('active');
      }
    }
  }

  filterByRole(items: NavigationItem[], roles: string[]): NavigationItem[] {
    return items
      .filter(item => !item.role || item.role.some(r => roles.includes(r)))
      .map(item => ({
        ...item,
        children: item.children ? this.filterByRole(item.children, roles) : undefined
      }))
      .filter(item => item.type !== 'collapse' || (item.children && item.children.length > 0));
  }

  // Méthode pour définir l'état actif par défaut
  private setDefaultActiveState() {
    setTimeout(() => {
      // Vérifier si aucun élément n'est actif
      const activeElements = document.querySelectorAll('.nav-item.active, .coded-hasmenu.active');
      
      if (activeElements.length === 0) {
        // Activer le tableau de bord par défaut
        const defaultDashboard = document.querySelector('.nav-item a[href="/default/"]')?.closest('.nav-item');
        if (defaultDashboard) {
          defaultDashboard.classList.add('active');
        }
      }
    }, 100);
  }
}
