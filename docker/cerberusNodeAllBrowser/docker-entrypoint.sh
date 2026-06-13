#!/bin/bash
set -e

if [ "${INSTALL_CERTIFICATE}" = "true" ]; then
    echo "Install Certificate..."
    mkdir -p "$HOME/.pki/nssdb"

    if [ ! -f "$HOME/.pki/nssdb/cert9.db" ]; then
        certutil -N -d sql:$HOME/.pki/nssdb --empty-password
    fi

    pk12util \
        -d sql:$HOME/.pki/nssdb \
        -i "${P12_FILE}" \
        -W "${P12_PASSWORD}"
fi

#if [ "${INSTALL_CHROME_POLICY}" = "true" ]; then
#    echo "Install Chrome policies..."
#    mkdir -p /etc/opt/chrome/policies/managed
#    cp "${POLICY_FILE}" /etc/opt/chrome/policies/managed/
#fi

exec /opt/bin/entry_point.sh "$@"
