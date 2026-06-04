import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule, NgClass } from '@angular/common';
import { AjouterFichierService } from './ajouter-fichier.service';

@Component({
  selector: 'app-liste-fichier',
  standalone: true,
  templateUrl: './liste-fichier.component.html',
  imports: [CommonModule, NgClass],
})
export class ListeFichierComponent implements OnInit {
  @Input() typeFichier: string = '';
  @Input() codeValeur: string = '';
  fichiers: any[] = [];
  @Output() fichierSelectionne = new EventEmitter<any>();

  constructor(private ajouterFichierService: AjouterFichierService) {}

  ngOnInit(): void {
    this.ajouterFichierService.getAllFichiers().subscribe(data => {
      this.ajouterFichierService.setFichiers(data);
      this.fichiers = this.filtrerFichiers(data);
    });

    this.ajouterFichierService.fichiers$.subscribe(data => {
      this.fichiers = this.filtrerFichiers(data);
    });
  }

  private filtrerFichiers(data: any[]): any[] {
    return (data || []).filter(f => {
      // Gère camelCase et SNAKE_CASE
      const type = f.typeFichier || f.TYPE_FICHIER;
      const code = f.codeValeur || f.CODE_VALEUR;
      return type === this.typeFichier && code === this.codeValeur;
    });
  }


}
