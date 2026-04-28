import { adminHttp } from '../utils/http'

export function getSyncConfig() {
  return adminHttp.get('/admin/csdn-sync/config')
}

export function saveSyncConfig(data) {
  return adminHttp.post('/admin/csdn-sync/config', data)
}

export function triggerSync() {
  return adminHttp.post('/admin/csdn-sync/sync')
}
