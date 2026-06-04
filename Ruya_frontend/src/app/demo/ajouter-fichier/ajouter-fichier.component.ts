import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import { AjouterFichierService } from './ajouter-fichier.service';
import { ApiService } from '../../services/api.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-ajouter-fichier',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './ajouter-fichier.component.html',
  styleUrls: ['./ajouter-fichier.component.scss']
})
export class AjouterFichierComponent implements OnInit, OnDestroy {
  isOpen: boolean = false;
  private subscription: Subscription = new Subscription();

  // Propriétés existantes
  nomFichier: string = '';
  typeFichier: string = '';
  formatFichier: string = '';
  codeFichier: string = '';
  codeEnregistrement: string = '';
  sens: string = '';
  montant: string = '';
  nombre: string = '';
  idUser: number = 1; // ID utilisateur par défaut

  // NOUVELLES PROPRIÉTÉS
  numeroRemise: string = '';
  validation: string = '';
  origineSaisie: string = 'WEB'; // Valeur par défaut
  typeEncaissement: string = '';

  // Propriété pour l'utilisateur connecté
  currentUser: any = null;

  typesFichier = [
    { value: 'cheque', label: 'Chèque', icon: 'ti ti-currency-dollar' },
    { value: 'effet', label: 'Effet', icon: 'ti ti-file-invoice' },
    { value: 'prelevement', label: 'Prélèvement', icon: 'ti ti-credit-card' },
    { value: 'virement', label: 'Virement', icon: 'ti ti-arrows-double-ne-sw' }
  ];

  codesFichier: { [key: string]: Array<{ value: string, label: string }> } = {
    cheque: [
      { value: '30', label: '30 - Remise cheque' },
      { value: '31', label: '31 - Cheque rejeté' },
      { value: '32', label: '32 - Cheque payé' },
      { value: '33', label: '33 - Cheque impayé' }
    ],
    effet: [
      { value: '40', label: '40 - Remise effet' },
      { value: '41', label: '41 - Effet rejeté' }
    ],
    prelevement: [{ value: '20', label: '20 - Prélèvement' }],
    virement: [{ value: '10', label: '10 - Virement' }]
  };

  sensOptions = [
    { value: 'emis', label: 'Émis', icon: 'ti ti-arrow-up' },
    { value: 'recu', label: 'Reçu', icon: 'ti ti-arrow-down' }
  ];

  codeEnregistrementOptions = [
    { value: '21', label: '21 - Présentation', icon: 'ti ti-check' },
    { value: '22', label: '22 - Rejet', icon: 'ti ti-x' }
  ];

  formatFichierOptions = [
    { value: 'env', label: '.ENV', icon: 'ti ti-file' },
    { value: 'rcp', label: '.RCP', icon: 'ti ti-file' }
  ];

  constructor(
    private ajouterFichierService: AjouterFichierService,
    private apiService: ApiService,
    private router: Router
  ) {}

  ngOnInit() {
    // Vérifier si l'utilisateur est connecté
    this.checkUserAuthentication();

    // S'abonner à l'état du modal
    this.subscription = this.ajouterFichierService.isModalOpen$.subscribe(
      isOpen => this.isOpen = isOpen
    );

    // Générer automatiquement un numéro de remise
    this.genererNumeroRemise();
  }

  /**
   * Vérifie si l'utilisateur est connecté et récupère ses informations
   */
  private checkUserAuthentication(): void {
    if (!this.ajouterFichierService.isUserLoggedIn()) {
      console.error('❌ Utilisateur non connecté');
      alert('Vous devez être connecté pour ajouter un fichier.');
      this.router.navigate(['/guest/login']);
      return;
    }

    this.currentUser = this.ajouterFichierService.getCurrentUser();
    if (!this.currentUser || !this.currentUser.id) {
      console.error('❌ Informations utilisateur invalides');
      alert('Erreur lors de la récupération des informations utilisateur.');
      this.router.navigate(['/guest/login']);
      return;
    }

    this.idUser = this.currentUser.id;
    console.log('✅ Utilisateur connecté:', this.currentUser.username);
    console.log('✅ ID utilisateur:', this.idUser);
  }

  ngOnDestroy() {
    this.subscription.unsubscribe();
  }

  /**
   * Génère automatiquement un numéro de remise au format REM-YYYY-XXX
   */
  genererNumeroRemise() {
    const annee = new Date().getFullYear();
    const numero = Math.floor(Math.random() * 999) + 1;
    this.numeroRemise = `REM-${annee}-${numero.toString().padStart(3, '0')}`;
  }

  getCodesDisponibles() {
    return this.codesFichier[this.typeFichier] || [];
  }

  onClose() {
    this.ajouterFichierService.closeModal();
  }

  onBackdropClick(event: Event) {
    if (event.target === event.currentTarget) {
      this.onClose();
    }
  }

  /**
   * Validation du numéro de remise
   */
  private validerNumeroRemise(): boolean {
    const regex = /^[A-Z]{2,4}-[0-9]{4}-[0-9]{3,6}$/;
    return regex.test(this.numeroRemise);
  }

  onSubmit() {
    try {
      // Vérifier si l'utilisateur est connecté
      if (!this.currentUser || !this.currentUser.id) {
        throw new Error('Vous devez être connecté pour ajouter un fichier.');
      }

      // Validation côté client améliorée
      if (!this.nomFichier || this.nomFichier.trim().length < 3) {
        throw new Error('Le nom du fichier doit contenir au moins 3 caractères.');
      }

      if (!this.typeFichier) {
        throw new Error('Veuillez sélectionner un type de fichier.');
      }

      if (!this.sens) {
        throw new Error('Veuillez sélectionner un sens.');
      }

      if (!this.montant || isNaN(Number(this.montant)) || Number(this.montant) <= 0) {
        throw new Error('Veuillez saisir un montant valide.');
      }

      if (!this.nombre || isNaN(Number(this.nombre)) || Number(this.nombre) <= 0) {
        throw new Error('Veuillez saisir un nombre valide.');
      }

      // Validation du numéro de remise
      if (!this.numeroRemise || !this.validerNumeroRemise()) {
        throw new Error('Veuillez saisir un numéro de remise valide (format: REM-YYYY-XXX).');
      }

      console.log('🔍 DEBUG - Utilisateur connecté:', this.currentUser);
      console.log('🔍 DEBUG - ID utilisateur:', this.currentUser.id);

      // 🔧 FIX: Utiliser le code fichier sélectionné (30/31/32/33/40/41/10/20)
      // au lieu du code générique (CHEQUE/EFFET/VIREMENT/PRELEVEMENT)
      const getCodeValeur = (): string => {
        // Si un code spécifique est sélectionné, l'utiliser
        if (this.codeFichier && this.codeFichier.trim()) {
          return this.codeFichier.trim();
        }
        
        // Sinon, fallback sur le code générique basé sur le type
        const valLower = (this.typeFichier || '').toLowerCase();
        switch (valLower) {
          case 'cheque':
            return '30'; // Code par défaut pour chèque remis
          case 'effet':
            return '40'; // Code par défaut pour effet remis
          case 'virement':
            return '10';
          case 'prelevement':
            return '20';
          default:
            return '30';
        }
      };

      // Mapping du typeFichier UI vers l'enum TypeFichier backend
      // TypeFichier backend: ELECTRONIQUE, WEB, EN_SAISIE, MANUEL
      const mapTypeFichier = (origine: string): string => {
        const origineLower = (origine || '').toLowerCase();
        switch (origineLower) {
          case 'web':
            return 'WEB';
          case 'agence':
            return 'MANUEL';
          case 'batch':
            return 'EN_SAISIE';
          case 'electronique':
            return 'ELECTRONIQUE';
          default:
            return 'ELECTRONIQUE';
        }
      };

      // Mapping pour les champs backend
      const mapNatureFichier = (val: string): string => {
        const valLower = (val || '').toLowerCase();
        switch (valLower) {
          case 'env':
            return 'REMISE';
          case 'rcp':
            return 'FICHIER';
          default:
            return 'FICHIER';
        }
      };

      const mapSens = (val: string): string => {
        const valLower = (val || '').toLowerCase();
        switch (valLower) {
          case 'emis':
            return 'SORTANT';
          case 'recu':
            return 'ENTRANT';
          default:
            return 'ENTRANT';
        }
      };

      const mapTypeEncaissement = (val: string): string => {
        const valLower = (val || '').toLowerCase();
        switch (valLower) {
          case 'immediat':
          case 'immédiat':
            return 'IMMEDIAT';
          case 'differe':
          case 'différé':
            return 'DIFFERE';
          default:
            return 'IMMEDIAT';
        }
      };

      const validationBool = (this.validation || '').toLowerCase().startsWith('valid');

      // Préparer les données - Structure compatible backend
      const fichierData = {
        nomFichier: this.nomFichier.trim(),
        typeFichier: mapTypeFichier(this.origineSaisie), // TypeFichier basé sur l'origine (ELECTRONIQUE, MANUEL, WEB, EN_SAISIE)
        natureFichier: mapNatureFichier(this.formatFichier), // NatureFichier (FICHIER, REMISE)
        codeValeur: getCodeValeur(), // ✅ Code numérique spécifique (30/31/32/33/40/41/10/20)
        codEn: this.codeEnregistrement || '21',
        sens: mapSens(this.sens), // Sens (ENTRANT, SORTANT)
        montant: parseFloat(this.montant) || 0,
        nomber: parseInt(this.nombre) || 0,
        numeroRemise: this.numeroRemise.trim(),
        typeEncaissement: this.typeEncaissement ? mapTypeEncaissement(this.typeEncaissement) : 'IMMEDIAT',
        origineSaisie: this.origineSaisie || 'WEB',
        validation: validationBool,
        user: {
          id: this.currentUser.id
        }
      };

      console.log('📤 Envoi du fichier:', fichierData);
      console.log('🔍 Type fichier UI:', this.typeFichier);
      console.log('🔍 Code sélectionné:', this.codeFichier);
      console.log('🔍 CodeValeur Backend:', fichierData.codeValeur);
      console.log('🔍 TypeFichier Backend (enum):', fichierData.typeFichier);
      console.log('🔍 NatureFichier Backend (enum):', fichierData.natureFichier);
      console.log('🔍 Sens Backend (enum):', fichierData.sens);
      console.log('🔍 User:', fichierData.user);

      // Créer le fichier via le service API
      this.apiService.createFichier(fichierData).subscribe({
        next: (response) => {
          console.log('✅ Fichier créé:', response);
          
          // Traiter la réponse du backend
          let message = 'Fichier ajouté avec succès !';
          if (response && response.message) {
            message = response.message;
          }
          
          // Mettre à jour la liste des fichiers
          this.ajouterFichierService.getAllFichiers().subscribe({
            next: (fichiers) => {
              this.ajouterFichierService.setFichiers(fichiers);
              this.ajouterFichierService.fichierAjoute$.next();
            },
            error: (err) => console.error('Erreur lors de la mise à jour de la liste:', err)
          });

          // Réinitialiser et fermer
          this.resetForm();
          this.ajouterFichierService.closeModal();
          alert(message);
        },
        error: (err) => {
          console.error('❌ Erreur complète:', err);
          console.error('❌ Status:', err.status);
          console.error('❌ Error object:', err.error);
          
          let message = 'Erreur lors de l\'ajout du fichier';
          
          // Traiter les différents types d'erreurs
          if (err.status === 400) {
            message = 'Requête invalide. Vérifiez les données saisies.';
            
            // Si le backend renvoie des détails
            if (err.error) {
              if (typeof err.error === 'string') {
                message = err.error;
              } else if (err.error.message) {
                message = err.error.message;
              } else if (err.error.error) {
                message = err.error.error;
              } else if (err.error.errors) {
                // Erreurs de validation
                const validationErrors = err.error.errors;
                message = 'Erreurs de validation:\n';
                for (const key in validationErrors) {
                  message += `- ${key}: ${validationErrors[key]}\n`;
                }
              }
            }
          } else if (err.status === 401) {
            message = 'Non autorisé. Veuillez vous reconnecter.';
          } else if (err.status === 500) {
            message = 'Erreur serveur. Contactez l\'administrateur.';
          } else if (err.error) {
            if (err.error.error) {
              message = err.error.error;
            } else if (err.error.message) {
              message = err.error.message;
            } else if (typeof err.error === 'string') {
              message = err.error;
            }
          } else if (err.message) {
            message = err.message;
          }
          
          console.error('📋 Message d\'erreur final:', message);
          alert('❌ ' + message);
        }
      });

    } catch (error) {
      alert(error.message);
    }
  }
  resetForm() {
    this.nomFichier = '';
    this.typeFichier = '';
    this.formatFichier = '';
    this.codeFichier = '';
    this.codeEnregistrement = '';
    this.sens = '';
    this.montant = '';
    this.nombre = '';
    
    // Réinitialiser les nouveaux champs
    this.validation = '';
    this.origineSaisie = 'WEB';
    this.typeEncaissement = '';
    
    // Générer un nouveau numéro de remise
    this.genererNumeroRemise();
  }



  /**
   * Méthode utilitaire pour vérifier si le formulaire est valide
   */
  isFormValid(): boolean {
    return !!(
      this.nomFichier && 
      this.typeFichier && 
      this.sens && 
      this.montant && 
      this.nombre && 
      this.numeroRemise &&
      this.validerNumeroRemise()
    );
  }
}