import { Component, Input, OnInit, OnChanges, SimpleChanges } from '@angular/core';
import { NgApexchartsModule } from 'ng-apexcharts';
import { ApexOptions } from 'ng-apexcharts';

@Component({
  selector: 'app-pie-chart',
  standalone: true,
  imports: [NgApexchartsModule],
  templateUrl: './pie-chart.component.html',
  styleUrls: ['./pie-chart.component.scss']
})
export class PieChartComponent implements OnInit, OnChanges {
  @Input() data: any;
  
  chartOptions: ApexOptions = {
    series: [],
    chart: {
      type: 'pie',
      height: 350
    },
    labels: ['REMIS', 'REJET', 'RENDU', 'EN ATTENTE'],
    colors: ['#10b981', '#ef4444', '#17a2b8', '#ffc107'],
    legend: {
      position: 'bottom',
      fontSize: '14px',
      fontFamily: 'Segoe UI, Arial, sans-serif'
    },
    dataLabels: {
      enabled: true,
      formatter: function (val: any, opts: any) {
        const total = opts.w.globals.seriesTotals[opts.seriesIndex];
        const percentage = parseFloat(val.toString());
        return total + ' (' + percentage.toFixed(1) + '%)';
      }
    },
    responsive: [{
      breakpoint: 480,
      options: {
        chart: {
          width: 200
        },
        legend: {
          position: 'bottom'
        }
      }
    }]
  };

  ngOnInit() {
    this.updateChartData();
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['data']) {
      this.updateChartData();
    }
  }

  private updateChartData() {
    if (this.data) {
      const series = [
        this.data.REMIS || 0,
        this.data.REJET || 0,
        this.data.RENDU || 0,
        this.data.EN_ATTENTE || 0
      ];
      
      this.chartOptions = {
        ...this.chartOptions,
        series: series
      };
    }
  }
} 