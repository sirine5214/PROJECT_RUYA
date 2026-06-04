import { Component, OnInit, OnChanges, Input } from '@angular/core';
import { NgApexchartsModule } from 'ng-apexcharts';
import { ApexOptions } from 'ng-apexcharts';

@Component({
  selector: 'app-line-chart',
  standalone: true,
  imports: [NgApexchartsModule],
  templateUrl: './line-chart.component.html',
  styleUrls: ['./line-chart.component.scss']
})
export class LineChartComponent implements OnInit, OnChanges {
  @Input() data: any;
  
  chartOptions: ApexOptions = {
    series: [
      {
        name: 'Fichiers',
        type: 'line',
        data: []
      },
      {
        name: 'Montants (k DT)',
        type: 'line',
        data: []
      }
    ],
    chart: {
      height: 350,
      type: 'line',
      toolbar: {
        show: true
      }
    },
    stroke: {
      width: [2, 2],
      curve: 'smooth'
    },
    colors: ['#3B82F6', '#10B981'],
    xaxis: {
      categories: ['Jan', 'Fév', 'Mar', 'Avr', 'Mai', 'Jun', 'Jul', 'Aoû', 'Sep', 'Oct', 'Nov', 'Déc']
    },
    yaxis: [
      {
        title: {
          text: 'Nombre de Fichiers'
        },
        labels: {
          style: {
            colors: '#3B82F6'
          }
        }
      },
      {
        opposite: true,
        title: {
          text: 'Montants (k DT)'
        },
        labels: {
          style: {
            colors: '#10B981'
          }
        }
      }
    ],
    tooltip: {
      shared: true,
      intersect: false,
      y: {
        formatter: function(val: any) {
          return val.toFixed(0);
        }
      }
    },
    legend: {
      position: 'top'
    },
    grid: {
      borderColor: '#e7e7e7',
      row: {
        colors: ['#f3f3f3', 'transparent'],
        opacity: 0.5
      }
    }
  };

  ngOnInit() {
    this.updateChartData();
  }

  ngOnChanges() {
    this.updateChartData();
  }

  updateChartData() {
    if (this.data && this.data.labels) {
      // Utiliser les vraies données de la base
      console.log('📊 Mise à jour du graphique avec les données réelles:', this.data);
      
      this.chartOptions.xaxis = {
        categories: this.data.labels || ['Jan', 'Fév', 'Mar', 'Avr', 'Mai', 'Jun', 'Jul', 'Aoû', 'Sep', 'Oct', 'Nov', 'Déc']
      };
      
      this.chartOptions.series = [
        {
          name: 'Fichiers',
          type: 'line',
          data: this.data.fichiers || []
        },
        {
          name: 'Montants (k DT)',
          type: 'line',
          data: this.data.montants || []
        }
      ];
    } else {
      // Fallback vers les données par défaut si pas de données
      console.log('⚠️ Utilisation des données par défaut');
      this.generateDefaultData();
    }
  }

  // Méthode de fallback avec données par défaut
  private generateDefaultData() {
    this.chartOptions.series = [
      {
        name: 'Fichiers',
        type: 'line',
        data: [30, 27, 30, 58, 54, 17, 56, 21, 30, 19, 30, 15]
      },
      {
        name: 'Montants (k DT)',
        type: 'line',
        data: [600, 500, 400, 1100, 850, 350, 650, 600, 500, 250, 450, 750]
      }
    ];
  }
} 