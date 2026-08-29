import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { finalize } from 'rxjs';
import { CertificationApiService } from '../../core/api/certification-api.service';
import { apiErrorMessage } from '../../core/http-error';
import { ToastService } from '../../core/toast/toast.service';
import { Dashboard } from '../../models/certification.models';
import { DonutChartComponent, DonutSegment } from '../../shared/donut-chart/donut-chart';
import { humanizeStatus } from '../../shared/status-label';

@Component({ selector:'app-dashboard-page', standalone:true, imports:[CommonModule,DonutChartComponent], templateUrl:'./dashboard.html', styleUrl:'./dashboard.css' })
export class DashboardPage {
  private readonly api=inject(CertificationApiService); private readonly toast=inject(ToastService);
  readonly data=signal<Dashboard|null>(null); readonly loading=signal(true); readonly refreshing=signal(false); readonly humanize=humanizeStatus;
  constructor(){this.load();}
  load():void{this.loading.set(true);this.api.dashboard().pipe(finalize(()=>this.loading.set(false))).subscribe({next:v=>this.data.set(v),error:e=>this.toast.error(apiErrorMessage(e,'Unable to load dashboard.'))});}
  evaluate():void{this.refreshing.set(true);this.api.evaluateLifecycle().pipe(finalize(()=>this.refreshing.set(false))).subscribe({next:r=>{this.toast.success(`Lifecycle evaluated: ${r.changedRecords} status changes, ${r.notificationsCreated} notifications.`);this.load();},error:e=>this.toast.error(apiErrorMessage(e,'Lifecycle evaluation failed.'))});}
  donut(d:Dashboard):DonutSegment[]{const colors:Record<string,string>={EXPIRED:'#ef4444',DAYS_0_30:'#f59e0b',DAYS_31_60:'#f97316',DAYS_61_90:'#38bdf8',SAFE:'#22c55e',NO_EXPIRY:'#8b5cf6'};return d.expiryDistribution.map(x=>({label:x.label,value:x.count,color:colors[x.key]||'#64748b'}));}
  badge(status:string):string{return 'badge badge-'+status.toLowerCase().replaceAll('_','-');}
}
