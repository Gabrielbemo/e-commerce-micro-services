import { Trend } from 'k6/metrics';

export const businessFlowDuration = new Trend('business_flow_duration', true);
