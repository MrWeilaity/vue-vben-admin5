import type { OnActionClickFn, VxeTableGridColumns } from '#/adapter/vxe-table';
import type { SystemOnlineApi } from '#/api/system/online';

import { useAccess } from '@vben/access';

import { $t } from '#/locales';

export function useColumns<T = SystemOnlineApi.OnlineUser>(
  onActionClick: OnActionClickFn<T>,
): VxeTableGridColumns<T> {
  const { hasAccessByCodes } = useAccess();

  return [
    { field: 'sessionId', title: $t('system.online.sessionId'), minWidth: 220 },
    { field: 'username', title: $t('system.online.username'), width: 140 },
    { field: 'deptName', title: $t('system.online.deptName'), width: 140 },
    { field: 'loginIp', title: $t('system.online.loginIp'), width: 140 },
    { field: 'loginAddress', title: $t('system.online.loginAddress'), minWidth: 180 },
    { field: 'browser', title: $t('system.online.browser'), width: 140 },
    { field: 'os', title: $t('system.online.os'), width: 140 },
    { field: 'deviceType', title: $t('system.online.deviceType'), width: 110 },
    { field: 'loginTime', title: $t('system.online.loginTime'), width: 180 },
    { field: 'lastAccessTime', title: $t('system.online.lastAccessTime'), width: 180 },
    { field: 'expiresAt', title: $t('system.online.expiresAt'), width: 180 },
    {
      align: 'center',
      cellRender: {
        attrs: {
          nameField: 'username',
          nameTitle: $t('system.online.username'),
          onClick: onActionClick,
        },
        name: 'CellOperation',
        options: [
          {
            code: 'offline',
            show: (row: T) =>
              hasAccessByCodes(['System:Online:Offline']) && !(row as SystemOnlineApi.OnlineUser).current,
            text: $t('system.online.offline'),
          },
        ],
      },
      field: 'operation',
      fixed: 'right',
      title: $t('system.online.operation'),
      width: 120,
    },
  ];
}
